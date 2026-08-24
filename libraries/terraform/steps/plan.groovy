// steps/plan.groovy
//
// AWS auth: none configured here — see steps/init.groovy for the IRSA note.

void call() {
    String targetDir  = config.infra_dir ?: '.'
    String tfVarsCred = config.tf_vars ?: 'NONE'
    boolean isDestroy = (params.ACTION == 'destroy') || (config.is_destroy ? config.is_destroy.toString().toBoolean() : false)

    String destroyFlag = isDestroy ? "-destroy" : ""
    def planName = "tfplan-${env.BUILD_ID}-${System.currentTimeMillis()}.tfplan"

    def executePlan = { String varFileFlag ->
        echo "Generating Terraform Plan..."
        dir(targetDir) {
            sh "terraform plan ${varFileFlag} ${destroyFlag} -out=${planName} -input=false"
            sh "terraform show -no-color ${planName} > ${planName}.txt"
            archiveArtifacts artifacts: "${planName}, ${planName}.txt", allowEmptyArchive: false
            env.TF_PLAN_FILE = planName
        }
    }

    if (tfVarsCred != 'NONE') {
        withCredentials([file(credentialsId: tfVarsCred, variable: 'TF_VARS_FILE')]) {
            executePlan("-var-file=${env.TF_VARS_FILE}")
        }
    } else {
        executePlan("")
    }
}
