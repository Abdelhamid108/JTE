// steps/governanceHooks.groovy — JTE Lifecycle Hooks for Terraform Governance

// ─────────────────────────────────────────────────────────────
// 1. STARTUP VALIDATION HOOK
// ─────────────────────────────────────────────────────────────
@Validate
void validateConfig() {
    if (!config.infra_dir)          { error "terraform: 'infra_dir' is required." }
    if (!config.target_environment) { error "terraform: 'target_environment' is required." }
    echo "terraform [@Validate]: config OK — dir='${config.infra_dir}', env='${config.target_environment}'"
}

// ─────────────────────────────────────────────────────────────
// 2. PRE-STEP INTERCEPTOR HOOK
// ─────────────────────────────────────────────────────────────
@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step

    if (currentStep == 'deploy') {
        if (env.SECURITY_POLICY_PASSED != 'true') {
            echo "terraform [@BeforeStep]: Security stage was not completed — enforcing Checkov scan now."
            checkov()
        } else {
            echo "terraform [@BeforeStep]: Security policy cleared. Proceeding with deploy."
        }
    } else if (currentStep == 'destroy') {
        if (params.ACTION != 'destroy') {
            error "terraform [@BeforeStep]: ACTION is '${params.ACTION}', not 'destroy'. Aborting."
        }
        echo "terraform [@BeforeStep]: ACTION=destroy confirmed. Proceeding with teardown."
    }
}

// ─────────────────────────────────────────────────────────────
// 3. POST-STEP AUDIT & LINEAGE HOOK
// ─────────────────────────────────────────────────────────────
@AfterStep
void onAfterStep() {
    String currentStep = hookContext?.step

    if (currentStep == 'deploy') {
        registerVersion(
            type:        'INFRASTRUCTURE',
            component:   config.component_name ?: 'eks-cluster',
            version:     env.APP_VERSION,
            environment: config.target_environment,
            status:      'ACTIVE'
        )
    } else if (currentStep == 'destroy') {
        registerVersion(
            type:        'INFRASTRUCTURE',
            component:   config.component_name ?: 'eks-cluster',
            version:     env.APP_VERSION,
            environment: config.target_environment,
            status:      'DESTROYED'
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 4. POST-PIPELINE CLEANUP HOOK
// ─────────────────────────────────────────────────────────────
@CleanUp
void cleanup() {
    sh "rm -f *.tfplan registry_tmp.json || true"
}
