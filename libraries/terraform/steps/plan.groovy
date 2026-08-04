// steps/plan.groovy
 void call () {
    String targetDir  = config.infra_dir ?: '.'
    String tfVarsCred = config.tf_vars ?: 'NONE'
    String cloudCreds = config.cloud_creds ?: 'NONE'
    boolean isDestroy = config.is_destroy ? config.is_destroy.toString().toBoolean() : false  

    def runPlan = {
        String destroyFlag = isDestroy ? "-destroy" : ""
        def planName = "tfplan-${env.BUILD_ID}-${System.currentTimeMillis()}.tfplan"

        def executePlan = { String varFileFlag ->
            println "Generating Terraform Plan ...."
            dir(targetDir){
                sh "terraform plan ${varFileFlag} ${destroyFlag} -out=${planName} -input=false"
                archiveArtifacts artifacts: planName, allowEmptyArchive: false
                env.TF_PLAN_FILE = planName
            }
        }

        if (tfVarsCred != 'NONE') {
            withCredentials([file(credentialsId: tfVarsCred, variable: 'TF_VARS_FILE')]) {
                executePlan("-var-file=\${TF_VARS_FILE}")
            }
        } else {
            executePlan("")
        }
    }

    if (cloudCreds != 'NONE') {
        withCredentials([usernamePassword(credentialsId: cloudCreds, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) { runPlan() }
    } else {
        runPlan()
    }
 }