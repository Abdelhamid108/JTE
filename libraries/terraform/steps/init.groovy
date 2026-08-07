// steps/init.groovy

void call() {  
    String targetDir = config.infra_dir ?: '.'
    String cloudCreds = config.cloud_creds ?: 'NONE'
    
    def runInit = {
            echo "Initializing Terraform..."
            dir(targetDir) {
                sh "terraform init -reconfigure"
            }
    }

    if (cloudCreds && cloudCreds != 'NONE') {
        withCredentials([aws(credentialsId: cloudCreds, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
            runInit()
        }
    } else {
        runInit()
    }
}