// steps/deploy.groovy
//
// Applies ONLY the exact plan binary produced by steps/plan.groovy in this
// same build — never re-plans. The @BeforeStep hook in governanceHooks.groovy
// enforces security policy clearance before this step executes.

void call() {
    if (!env.TF_PLAN_FILE) {
        error "deploy: No plan file found. Run the Plan stage first."
    }
    dir(config.infra_dir ?: '.') {
        unstash 'tfplan'
        sh "terraform apply ${env.TF_PLAN_FILE}"
    }
}
