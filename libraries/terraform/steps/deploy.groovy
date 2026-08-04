// steps/deploy.groovy
void call () {
    String targetDir = config.infra_dir ?: '.'
    String cloudCreds = config.cloud_creds ?: 'NONE'

    echo "Applying terraform plan ...."
    def runDeploy = {
        dir(targetDir){
            if (env.TF_PLAN_FILE){
                sh "terraform apply ${env.TF_PLAN_FILE}"
            }else{
                error "No Plan Found in the environment, Run Plan step first"
            }
        }
    }
    if (cloudCreds != 'NONE') {
        withCredentials([usernamePassword(credentialsId: cloudCreds, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) { runDeploy() }
    } else {
        runDeploy()
    }
}
