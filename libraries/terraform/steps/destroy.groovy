// steps/destroy.groovy — Apply a Terraform destroy plan

void call() {
    if (!env.TF_PLAN_FILE) {
        error "terraform/destroy: No plan file found. Run plan() first."
    }
    dir(config.infra_dir) {
        unstash 'tfplan'
        sh "terraform apply ${env.TF_PLAN_FILE}"
    }
}
