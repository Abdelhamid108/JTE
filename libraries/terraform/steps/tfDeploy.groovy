// steps/tfDeploy.groovy

void call() {
    String targetDir  = config.infra_dir ?: '.'
    String cloudCreds = config.cloud_creds

    echo "Applying terraform plan..."
    def runDeploy = {
        dir(targetDir) {
            sh "terraform apply ${env.TF_PLAN_FILE}"
        }
    }

    if (cloudCreds) {
        withCredentials([aws(credentialsId: cloudCreds, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
            runDeploy()
        }
    } else {
        runDeploy()
    }
}
