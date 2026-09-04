// steps/plan.groovy — Generate a Terraform speculative plan and stash the binary

void call() {
    String planName    = "tfplan-${env.BUILD_ID}.tfplan"
    String destroyFlag = (params.ACTION == 'destroy') ? '-destroy' : ''

    dir(config.infra_dir) {
        if (config.tf_vars) {
            withCredentials([file(credentialsId: config.tf_vars, variable: 'TF_VARS_FILE')]) {
                sh "terraform plan -var-file=${env.TF_VARS_FILE} ${destroyFlag} -out=${planName} -input=false"
            }
        } else {
            sh "terraform plan ${destroyFlag} -out=${planName} -input=false"
        }

        sh "terraform show -no-color ${planName} > ${planName}.txt"
        archiveArtifacts artifacts: "${planName}, ${planName}.txt", allowEmptyArchive: false
        stash name: 'tfplan', includes: planName
        env.TF_PLAN_FILE = planName
    }
}
