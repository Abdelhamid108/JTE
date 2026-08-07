 // steps/destroy.groovy
 void call () {
    String targetDir = config.infra_dir ?: '.'
    String cloudCreds = config.cloud_creds ?: 'NONE'

    echo "Destroying infrastructure..."
    def runDestroy = {
        dir (targetDir){
            if(env.TF_PLAN_FILE){
                sh "terraform apply ${env.TF_PLAN_FILE}"
            }else{
                error "No Plan Found in the environment, Run Plan step first"
            }
        }
    }
    if (cloudCreds && cloudCreds != 'NONE') {
        withCredentials([aws(credentialsId: cloudCreds, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) { runDestroy() }
    } else {
        runDestroy()
    }

 }