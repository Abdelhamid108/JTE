// steps/governanceHooks.groovy — JTE Lifecycle Hooks for Terraform Governance

@Validate
void validateConfig() {
    if (!config.infra_dir)          { error "terraform: 'infra_dir' is required in pipeline_config.groovy." }
    if (!config.target_environment) { error "terraform: 'target_environment' is required in pipeline_config.groovy." }
    echo "terraform [@Validate]: config OK — dir='${config.infra_dir}', env='${config.target_environment}'"
}

@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step

    if (currentStep == 'init') {
        if (params.ACTION != 'destroy') {
            echo "terraform [@BeforeStep]: Enforcing Version Gate policy before init/plan..."
            versionGate(
                type:        config.artifact_type ?: 'INFRASTRUCTURE',
                component:   config.component_name ?: 'infrastructure',
                environment: config.target_environment
            )
        }
    } else if (currentStep == 'deploy') {
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

@AfterStep
void onAfterStep() {
    if (hookContext?.exceptionThrown) {
        echo "terraform [@AfterStep]: Step '${hookContext?.step}' threw an exception/failed — skipping version registration."
        return
    }

    String currentStep = hookContext?.step

    if (currentStep == 'deploy') {
        registerVersion(
            type:        config.artifact_type ?: 'INFRASTRUCTURE',
            component:   config.component_name ?: 'infrastructure',
            version:     env.APP_VERSION,
            environment: config.target_environment,
            status:      'ACTIVE'
        )
    } else if (currentStep == 'destroy') {
        registerVersion(
            type:        config.artifact_type ?: 'INFRASTRUCTURE',
            component:   config.component_name ?: 'infrastructure',
            version:     env.APP_VERSION,
            environment: config.target_environment,
            status:      'DESTROYED'
        )
    }
}

@CleanUp
void cleanup() {
    echo "terraform [@CleanUp]: Teardown completed."
}
