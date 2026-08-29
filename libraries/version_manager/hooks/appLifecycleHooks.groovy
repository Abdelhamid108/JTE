// libraries/version_manager/hooks/appLifecycleHooks.groovy

// ─────────────────────────────────────────────────────────────
// 1. GLOBAL STARTUP VALIDATION HOOK
// ─────────────────────────────────────────────────────────────
@Validate
void validateEnvironment() {
    echo "════════════════════════════════════════════════════"
    echo "  GLOBAL STARTUP VALIDATION [@Validate Hook]"
    echo "════════════════════════════════════════════════════"

    List requiredTools = ['mvn', 'docker', 'aws', 'trivy']
    requiredTools.each { tool ->
        int status = sh(script: "which ${tool} >/dev/null 2>&1", returnStatus: true)
        if (status != 0) {
            error "appLifecycleHooks [@Validate]: Required CLI tool '${tool}' is missing on agent."
        }
        echo "  ✓ CLI tool verified: ${tool}"
    }

    String registryPath = config.registry_path
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

    // Policy: Block image build if quality gate is enforced and not passed.
    // Only active when enforce_quality_gate = true in pipeline_config.
    // Non-blocking Sonar policy (enforce_quality_gate = false) skips this check.
    if (currentStep == 'buildImage' && config.enforce_quality_gate?.toBoolean()) {
        String gateStatus = env.SONAR_QUALITY_GATE_STATUS
        if (gateStatus != 'OK') {
            error "appLifecycleHooks [@BeforeStep]: Quality Gate status is '${gateStatus ?: 'unset'}'. Blocking image build — fail closed."
        }
    }

    // Policy: Block mutable ':latest' tag on any push or promote operation.
    if (currentStep == 'pushImage' || currentStep == 'promoteImage') {
        if (hookContext?.args?.environment == 'latest') {
            error "appLifecycleHooks [@BeforeStep]: Policy Violation — mutable tag ':latest' is prohibited."
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 3. POST-STEP QUALITY & GOVERNANCE REGISTRATION HOOKS
// ─────────────────────────────────────────────────────────────
@AfterStep
void onAfterStep() {
    String currentStep = hookContext?.step

    // Enforce JaCoCo code coverage threshold after verify()
    if (currentStep == 'verify') {
        echo "appLifecycleHooks [@AfterStep 'verify']: Auditing code coverage..."
        String appDir       = config.app_dir ?: '.'
        String jacocoReport = (appDir != '.') ? "${appDir}/target/site/jacoco/jacoco.csv" : "target/site/jacoco/jacoco.csv"
        int threshold       = (config.coverage_threshold ?: 80) as Integer

        if (!fileExists(jacocoReport)) {
            error "appLifecycleHooks [@AfterStep]: JaCoCo report not found at '${jacocoReport}'. Failing closed."
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
        long total   = missed + covered
        double pct   = total > 0 ? (covered * 100.0 / total) : 0.0
        echo "  Instruction coverage: ${String.format('%.2f', pct)}% (threshold: ${threshold}%)"

        if (pct < threshold) {
            error "appLifecycleHooks [@AfterStep]: Coverage ${String.format('%.2f', pct)}% is below ${threshold}%. Failing build."
        }
        echo "  ✓ Coverage threshold met."
    }

    // Auto-register version in S3 after successful push or promote
    if (currentStep == 'pushImage' || currentStep == 'promoteImage') {
        String environment = hookContext?.args?.environment ?: config.target_environment
        echo "appLifecycleHooks [@AfterStep '${currentStep}']: Registering ${config.artifact_type} ${config.component_name}@${env.APP_VERSION} -> '${environment}' in S3..."
        registerVersion(environment: environment)
        echo "  ✓ S3 Version Registry updated for '${environment}'."
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
        sh "docker stop validate-${env.BUILD_ID} 2>/dev/null || true"
        sh "docker rm -f validate-${env.BUILD_ID} 2>/dev/null || true"
        sh "docker image prune -f --filter 'label=stage=builder' 2>/dev/null || true"
        echo "  ✓ Ephemeral containers and dangling layers pruned."
    } catch (Exception e) {
        echo "  ⚠️ Cleanup warning: ${e.message}"
    }
}
