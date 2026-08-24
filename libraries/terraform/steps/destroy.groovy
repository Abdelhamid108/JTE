// steps/destroy.groovy
//
// Applies a plan file, and ONLY a plan file that was itself generated with
// -destroy (steps/plan.groovy sets that flag from params.ACTION=='destroy'
// or config.is_destroy). This step must never be reachable from the normal
// deploy path of a pipeline template — templates must gate it behind an
// explicit destroy action AND steps/approval.groovy.

void call() {
    String targetDir = config.infra_dir ?: '.'

    boolean isDestroy = (params.ACTION == 'destroy') || (config.is_destroy ? config.is_destroy.toString().toBoolean() : false)
    if (!isDestroy) {
        error "terraform/destroy: refusing to run — this build was not explicitly parameterized/configured for destroy (params.ACTION != 'destroy' and config.is_destroy != true)."
    }

    echo "Destroying infrastructure..."
    dir(targetDir) {
        if (env.TF_PLAN_FILE) {
            sh "terraform apply ${env.TF_PLAN_FILE}"
        } else {
            error "No Plan Found in the environment, Run Plan step first"
        }
    }
}
