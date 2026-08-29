// steps/destroy.groovy
//
// Applies a destroy plan file produced by steps/plan.groovy.
// Unstashes the plan artifact before applying.

void call() {
    String targetDir = config.infra_dir ?: '.'

    boolean isDestroy = (params.ACTION == 'destroy') || (config.is_destroy ? config.is_destroy.toString().toBoolean() : false)
    if (!isDestroy) {
        error "terraform/destroy: refusing to run — this build was not explicitly configured for destroy (params.ACTION != 'destroy')."
    }

    if (!env.TF_PLAN_FILE) {
        error "destroy: No Plan Found in the environment. Run Plan step first."
    }

    echo "Destroying infrastructure..."
    dir(targetDir) {
        unstash 'tfplan'
        sh "terraform apply ${env.TF_PLAN_FILE}"
    }
}
