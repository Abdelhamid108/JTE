// steps/deploy.groovy
def call (Map config = [:]) {
    def targetDir = config.dir ?: '.'

    println "Applying terraform plan ...."

    dir(targetDir){
        if (env.TF_PLAN_FILE){
            sh "terraform apply ${env.TF_PLAN_FILE}"
        }else{
            error "No Plan Found in the environment, Run Plan step first"
        }
    }
}
