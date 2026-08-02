 // steps/destroy.groovy
 void call () {
    String targetDir = config.infra_dir ?: '.'
    String cloudCreds = config.cloud_creds ?: 'NONE'

    echo "Destroying infrastructure..."
    def runDestroy {
        dir (targetDir){
            if(env.TF_PLAN_FILE){
                sh "terraform apply ${env.TF_PLAN_FILE}"
            }else{
                error "No Plan Found in the envrionment, Run Plan step first"
            }
        }
    }
    if ( cloud_creds != 'NONE' ){
        withCredentials([string(credentialsId: cloudCreds, variable: 'CLOUD_TOKEN')]) { runDestroy() }
    } else {
        runDestroy ()
    }

 }