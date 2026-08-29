// steps/governanceHooks.groovy — JTE Lifecycle Hooks for Terraform Governance
// NOTE: JTE injects @Validate, @BeforeStep, @AfterStep, @CleanUp into the
// sandbox automatically. Do NOT import them — the Groovy classloader cannot
// resolve them as explicit classes and will fail at parse time.

// Validates required configuration fields before any stage runs.
@Validate
void validateConfig() {
    if (!config.infra_dir)          { error "terraform: 'infra_dir' is required." }
    if (!config.target_environment) { error "terraform: 'target_environment' is required." }
    echo "terraform [@Validate]: config OK — dir='${config.infra_dir}', env='${config.target_environment}'"
} 

// Blocks deploy() if the Security Policy Gate stage did not complete in this run.
// If SECURITY_POLICY_PASSED is not set (stage was bypassed), Checkov is enforced here.
@BeforeStep(step = 'deploy')
void beforeDeploy() {
    if (env.SECURITY_POLICY_PASSED != 'true') {
        echo "terraform [@BeforeStep]: Security stage was not completed — enforcing Checkov scan now."
        checkov()
    } else {
        echo "terraform [@BeforeStep]: Security policy cleared. Proceeding with deploy."
    }
}

// Guards destroy() against accidental invocation from a misconfigured ACTION parameter.
@BeforeStep(step = 'destroy')
void beforeDestroy() {
    if (params.ACTION != 'destroy') {
        error "terraform [@BeforeStep]: ACTION is '${params.ACTION}', not 'destroy'. Aborting."
    }
    echo "terraform [@BeforeStep]: ACTION=destroy confirmed. Proceeding with teardown."
}

// Records ACTIVE infrastructure state in the S3 version registry after apply.
@AfterStep(step = 'deploy')
void afterDeploy() {
    registerVersion(
        type:        'INFRASTRUCTURE',
        component:   config.component_name ?: 'eks-cluster',
        version:     env.APP_VERSION,
        environment: config.target_environment,
        status:      'ACTIVE'
    )
}

// Records DESTROYED infrastructure state in the S3 version registry after teardown.
@AfterStep(step = 'destroy')
void afterDestroy() {
    registerVersion(
        type:        'INFRASTRUCTURE',
        component:   config.component_name ?: 'eks-cluster',
        version:     env.APP_VERSION,
        environment: config.target_environment,
        status:      'DESTROYED'
    )
}

// Removes transient plan binaries after the pipeline completes.
@CleanUp
void cleanup() {
    sh "rm -f *.tfplan registry_tmp.json || true"
}
