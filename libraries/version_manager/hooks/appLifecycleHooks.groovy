// libraries/version_manager/hooks/appLifecycleHooks.groovy

// ─────────────────────────────────────────────────────────────
// 1. GLOBAL STARTUP VALIDATION HOOK
// ─────────────────────────────────────────────────────────────
@Validate
void validateEnvironment() {
    echo "════════════════════════════════════════════════════"
    echo "  GLOBAL STARTUP VALIDATION [@Validate Hook]"
    echo "════════════════════════════════════════════════════"

    // 1. Assert CLI Tools Exist
    List requiredTools = ['mvn', 'docker', 'aws', 'trivy']
    requiredTools.each { tool ->
        int status = sh(script: "which ${tool} >/dev/null 2>&1", returnStatus: true)
        if (status != 0) {
            error "appLifecycleHooks [@Validate]: Required CLI tool '${tool}' is missing on agent."
        }
        echo "  ✓ CLI tool verified: ${tool}"
    }

    // 2. Assert S3 Version Registry Bucket is Reachable
    String registryPath = config.registry_path ?: pipelineConfig?.libraries?.version_manager?.registry_path
    if (registryPath) {
        int s3Status = sh(script: "aws s3 ls ${registryPath} >/dev/null 2>&1 || aws s3 cp ${registryPath} - >/dev/null 2>&1", returnStatus: true)
        if (s3Status != 0) {
            echo "  ⚠️ Warning: Version registry at '${registryPath}' not reachable or bucket is empty. Will initialize on first write."
        } else {
            echo "  ✓ S3 Version Registry reachable: ${registryPath}"
        }
    }

    echo "════════════════════════════════════════════════════"
}

// ─────────────────────────────────────────────────────────────
// 2. PRE-STEP POLICY INTERCEPTOR HOOKS
// ─────────────────────────────────────────────────────────────
@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step

    // Policy 1: Assert SonarQube Quality Gate passed before packaging release container (Fail closed)
    if (currentStep == 'buildImage') {
        String gateStatus = env.SONAR_QUALITY_GATE_STATUS
        if (!gateStatus) {
            error "appLifecycleHooks [@BeforeStep]: SonarQube Quality Gate status is unset. 'scan()' must record env.SONAR_QUALITY_GATE_STATUS before buildImage() executes. Aborting — fail closed."
        }
        if (gateStatus != 'OK') {
            error "appLifecycleHooks [@BeforeStep]: SonarQube Quality Gate failed with status '${gateStatus}'. Aborting container build."
        }
    }

    // Policy 2: Sanitize Tag Policy on Image Promotion / Push (Block :latest, Enforce immutable tags)
    if (currentStep == 'promoteDockerImage' || currentStep == 'pushImage') {
        String targetImage = hookContext?.args?.target_image ?: hookContext?.args?.image_uri ?: ''
        if (targetImage.endsWith(':latest')) {
            error "appLifecycleHooks [@BeforeStep]: Policy Violation! Mutable tag ':latest' is prohibited to prevent Argo CD Image Updater race conditions."
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 3. POST-STEP QUALITY & COVERAGE AUDIT HOOKS
// ─────────────────────────────────────────────────────────────
@AfterStep
void onAfterStep() {
    String currentStep = hookContext?.step

    // Audit and Enforce JaCoCo Code Coverage after verify()
    if (currentStep == 'verify') {
        echo "appLifecycleHooks [@AfterStep 'verify']: Auditing test integrity and code coverage..."
        String appDir = config.app_dir ?: 'application'
        String jacocoReport = "${appDir}/target/site/jacoco/jacoco.csv"
        int threshold = (config.coverage_threshold ?: pipelineConfig?.libraries?.version_manager?.coverage_threshold ?: 80) as Integer

        if (!fileExists(jacocoReport)) {
            error "appLifecycleHooks [@AfterStep]: JaCoCo CSV report not found at '${jacocoReport}'. Cannot verify coverage threshold — failing closed."
        }

        List<String> lines = readFile(jacocoReport).trim().split('\n') as List
        long missed  = 0
        long covered = 0
        lines.drop(1).each { line ->
            List cols = line.split(',')
            if (cols.size() > 4) {
                missed  += (cols[3] as Long)
                covered += (cols[4] as Long)
            }
        }
        long total = missed + covered
        double pct = total > 0 ? (covered * 100.0 / total) : 0.0
        echo "  Instruction coverage: ${String.format('%.2f', pct)}% (required threshold: ${threshold}%)"

        if (pct < threshold) {
            error "appLifecycleHooks [@AfterStep]: Coverage ${String.format('%.2f', pct)}% is below required threshold of ${threshold}%. Failing build."
        }
        echo "  ✓ Coverage threshold met successfully."
    }
}

// ─────────────────────────────────────────────────────────────
// 4. GLOBAL TEARDOWN & RESOURCE HYGIENE HOOK
// ─────────────────────────────────────────────────────────────
@CleanUp
void onCleanUp() {
    echo "════════════════════════════════════════════════════"
    echo "  GLOBAL TEARDOWN [@CleanUp Hook]"
    echo "════════════════════════════════════════════════════"
    try {
        String containerName = "validate-${env.BUILD_ID}"
        sh "docker stop ${containerName} 2>/dev/null || true"
        sh "docker rm -f ${containerName} 2>/dev/null || true"
        sh "docker image prune -f --filter 'label=stage=builder' 2>/dev/null || true"
        echo "  ✓ Ephemeral test containers and dangling layers pruned."
    } catch (Exception e) {
        echo "  ⚠️ Cleanup warning: ${e.message}"
    }
}
