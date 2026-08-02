// steps/plan.groovy
 void call () {
    // defaults to current directory if not provided
    String targetDir = config.infra_dir ?: '.'
    String varFile = config.tf_vars ? "-var-file=${config.tf_vars}" : ""
    String cloudCreds = config.cloud_creds ?: 'NONE'
    boolean isDestroy = config.is_destroy ? config.is_destroy.toString().toBoolean() : false  

    def runPlan = {
        String destroyFlag = isDestroy ? "-destroy" : "" //check if the flage isDestroy - true to run with -destroy flag

        def planName = "tfplan-${env.BUILD_ID}-${System.currentTimeMillis()}.tfplan"

        println "Generating Trraform Plan ...."
        dir(targetDir){
            sh "terraform plan ${varFile} ${isDestroy} -out=${planName} -input=false"
            archiveArtifacts artifacts: planName, allowEmptyArchive: false
            env.TF_PLAN_FILE = planName
        }
    }
    if ( cloudCreds != 'NONE'){
        withCredentials([string(credentialsId: cloudCreds, variable: 'CLOUD_TOKEN')]) { runPlan() }
    } else{
        runPlan ()
    }
 }