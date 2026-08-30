// hooks/appLifecycleHooks.groovy — JTE Lifecycle Hooks for Version Management & Strict Quality Governance

// ─────────────────────────────────────────────────────────────
// PRE-STEP: Strict Quality, Security & Version Gates
// ─────────────────────────────────────────────────────────────
@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step
    String branch      = env.BRANCH_NAME

    // ── GUARD 1: Build image ONLY if tests, coverage, Trivy FS, and SonarQube passed
    if (currentStep == 'buildImage') {
        echo "appLifecycleHooks [@BeforeStep 'buildImage']: Enforcing prerequisite Quality & Security gates..."
        if (env.STAGE_TEST_PASSED != 'true') {
            error "appLifecycleHooks: Prerequisite 'verify' (tests & 80% code coverage) has not completed successfully."
        }
        if (env.STAGE_FS_SCAN_PASSED != 'true') {
            error "appLifecycleHooks: Prerequisite 'scanFilesystem' (Trivy dependency scan) has not completed successfully."
        }
        if (env.STAGE_SONAR_PASSED != 'true') {
            error "appLifecycleHooks: Prerequisite 'scan' (SonarQube Quality Gate) has not completed successfully."
        }
        echo "appLifecycleHooks: [PASSED] Code Quality & Security prerequisites verified (Tests, 80% Coverage, Trivy FS, SonarQube)."
    }

    // ── GUARD 2: Push image ONLY if container validation and image security scan passed
    if (currentStep == 'push' && branch == 'dev') {
        echo "appLifecycleHooks [@BeforeStep 'push']: Enforcing container smoke validation & image security scan..."
        if (env.STAGE_CONTAINER_VALIDATE_PASSED != 'true') {
            error "appLifecycleHooks: Prerequisite 'containerValidate' (Container Smoke Test) has not passed."
        }
        if (env.STAGE_IMAGE_SCAN_PASSED != 'true') {
            error "appLifecycleHooks: Prerequisite 'scanImage' (Trivy Image Vulnerability Scan) has not passed."
        }
        echo "appLifecycleHooks: [PASSED] Container validation and image security prerequisites verified."
    }

}

// ─────────────────────────────────────────────────────────────
// POST-STEP: S3 version registration
// ─────────────────────────────────────────────────────────────
@AfterStep
void onAfterStep() {
    if (hookContext?.exceptionThrown) {
        echo "appLifecycleHooks [@AfterStep]: Step '${hookContext?.step}' threw an exception/failed — skipping S3 version registration."
        return
    }

    String currentStep = hookContext?.step
    String branch      = env.BRANCH_NAME

    // ── EARLY FAIL-FAST: Enforce version gate immediately after reading the version.
    // Skip for destroy runs — a version must already be ACTIVE to be destroyed,
    // so the gate would always block teardown incorrectly.
    if (currentStep == 'readVersion' && params.ACTION != 'destroy') {
        String environment = ['dev': 'dev', 'test': 'test', 'main': 'prod'][branch]
        if (environment) {
            echo "appLifecycleHooks [@AfterStep 'readVersion']: Early fail-fast — Enforcing Version Gate for '${environment}'..."
            versionGate(environment: environment)
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
