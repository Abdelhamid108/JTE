 // steps/destroy.groovy
 def call (Map config = [:]){
    def targetDir = config.dir ?: '.'

    println "Destroying infrastructure..."

    dir (targetDir){
        if(env.TF_PLAN_FILE){
            sh "terraform apply ${env.TF_PLAN_FILE}"
        }else{
            error "No Plan Found in the envrionment, Run Plan step first"
        }
    }

 }