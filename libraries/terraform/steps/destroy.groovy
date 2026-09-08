// steps/destroy.groovy — Execute direct Terraform destroy

void call() {
    String targetDir = config.infra_dir ?: '.'
    echo "terraform/destroy: Executing terraform destroy..."
    dir(targetDir) {
        if (config.tf_vars) {
            withCredentials([file(credentialsId: config.tf_vars, variable: 'TF_VARS_FILE')]) {
                sh "terraform destroy -var-file=${env.TF_VARS_FILE} -auto-approve -input=false"
            }
        } else {
            sh "terraform destroy -auto-approve -input=false"
        }
    }
}

