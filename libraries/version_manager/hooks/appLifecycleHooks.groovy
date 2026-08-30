// libraries/version_manager/hooks/appLifecycleHooks.groovy

// ─────────────────────────────────────────────────────────────
// PRE-STEP: Version gate + Quality Gate guard
// ─────────────────────────────────────────────────────────────
@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step

    // Block image build if SonarQube Quality Gate failed
    if (currentStep == 'buildImage' && config.enforce_quality_gate?.toBoolean()) {
        String gateStatus = env.SONAR_QUALITY_GATE_STATUS
        if (gateStatus != 'OK') {
            error "appLifecycleHooks: Quality Gate is '${gateStatus ?: 'unset'}' — blocking image build."
        }
    }

    // Enforce version gate before any publish operation
    if (currentStep == 'push' || currentStep == 'promoteDockerImage') {
        String environment = ['dev': 'dev', 'test': 'test', 'main': 'prod'][env.BRANCH_NAME]
        if (environment) {
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

    // Enforce JaCoCo coverage threshold after verify()
    if (currentStep == 'verify') {
        String appDir       = config.app_dir ?: '.'
        String jacocoReport = "${appDir}/target/site/jacoco/jacoco.csv".replaceAll('^\\./','')
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
        echo "appLifecycleHooks: Coverage ${String.format('%.2f', pct)}% (threshold: ${threshold}%)"

        if (pct < threshold) {
            error "appLifecycleHooks: Coverage ${String.format('%.2f', pct)}% is below ${threshold}%."
        }
    }

    // Register version in S3 after push or promote
    if (currentStep == 'push' || currentStep == 'promoteDockerImage') {
        String environment = env.BRANCH_NAME
        echo "appLifecycleHooks: Registering ${config.component_name}@${env.APP_VERSION} -> '${environment}' in S3..."
        registerVersion(environment: environment)
    }
}
