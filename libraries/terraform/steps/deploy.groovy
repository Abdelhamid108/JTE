// steps/deploy.groovy
//
// AWS auth: none configured here — see steps/init.groovy for the IRSA note.
// Applies ONLY the exact plan file produced by steps/plan.groovy in this
// same build; it never re-plans.

void call() {
    String targetDir = config.infra_dir ?: '.'

    echo "Applying terraform plan..."
    dir(targetDir) {
        if (env.TF_PLAN_FILE) {
            sh "terraform apply ${env.TF_PLAN_FILE}"
        } else {
            error "No Plan Found in the environment, Run Plan step first"
        }
    }
}
