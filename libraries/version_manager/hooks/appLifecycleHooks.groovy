// libraries/version_manager/hooks/appLifecycleHooks.groovy

// ─────────────────────────────────────────────────────────────
// PRE-STEP: Version gate + Quality Gate guard
// ─────────────────────────────────────────────────────────────
@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step
    String branch      = env.BRANCH_NAME

    // Block image build if SonarQube Quality Gate failed (check sonar library config)
    boolean enforceSonar = pipelineConfig.libraries?.sonar?.enforce_quality_gate?.toBoolean() ?: false
    if (currentStep == 'buildImage' && enforceSonar) {
        String gateStatus = env.SONAR_QUALITY_GATE_STATUS
        echo "appLifecycleHooks [@BeforeStep 'buildImage']: Checking SonarQube Quality Gate status (${gateStatus ?: 'UNSET'})..."
        if (gateStatus != 'OK') {
            error "appLifecycleHooks: Quality Gate status is '${gateStatus ?: 'unset'}' — blocking image build."
        }
    }

    // Enforce version gate before any publish/promote step
    if (currentStep == 'push' || currentStep == 'promoteDockerImage') {
        String environment = ['dev': 'dev', 'test': 'test', 'main': 'prod'][branch]
        if (environment) {
            echo "appLifecycleHooks [@BeforeStep '${currentStep}']: Enforcing Version Gate for '${environment}'..."
            versionGate(environment: environment)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// POST-STEP: Coverage threshold + S3 version registration
// ─────────────────────────────────────────────────────────────
@AfterStep
void onAfterStep() {
    String currentStep = hookContext?.step
    String branch      = env.BRANCH_NAME

    // Enforce JaCoCo coverage threshold after verify()
    if (currentStep == 'verify') {
        echo "appLifecycleHooks [@AfterStep 'verify']: Auditing JaCoCo code coverage..."
        String appDir       = config.app_dir ?: '.'
        String jacocoReport = "${appDir}/target/site/jacoco/jacoco.csv".replaceAll('^\\./', '')
        int    threshold    = (config.coverage_threshold ?: 80) as Integer

        if (!fileExists(jacocoReport)) {
            error "appLifecycleHooks: JaCoCo report not found at '${jacocoReport}'."
        }

        long missed = 0, covered = 0
        readFile(jacocoReport).trim().split('\n').drop(1).each { line ->
            List cols = line.split(',')
            if (cols.size() > 4) { missed += cols[3] as Long; covered += cols[4] as Long }
        }
        double pct = (missed + covered) > 0 ? (covered * 100.0 / (missed + covered)) : 0.0
        echo "appLifecycleHooks: Instruction coverage: ${String.format('%.2f', pct)}% (threshold: ${threshold}%)"

        if (pct < threshold) {
            error "appLifecycleHooks: Coverage ${String.format('%.2f', pct)}% is below threshold of ${threshold}%."
        }
    }

    // Register version in S3 after push or promote.
    // Status is PUBLISHED — image is in ECR and available.
    // Argo CD Notifications will update status to ACTIVE via webhook once the app is healthy.
    if (currentStep == 'push' || currentStep == 'promoteDockerImage') {
        String environment = ['dev': 'dev', 'test': 'test', 'main': 'prod'][branch]
        if (environment) {
            echo "appLifecycleHooks [@AfterStep '${currentStep}']: Registering ${config.component_name}@${env.APP_VERSION} -> '${environment}' [PUBLISHED] in S3..."
            registerVersion(environment: environment, status: 'PUBLISHED')
        }
    }
}
