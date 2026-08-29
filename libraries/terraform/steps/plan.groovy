// steps/plan.groovy
//
// Generates a speculative Terraform execution plan, archives it as a build
// artifact, and stashes the binary for guaranteed apply integrity — ensuring
// deploy() applies the exact diff reviewed in the Approval Guardrail stage.

void call() {
    String targetDir  = config.infra_dir ?: '.'
    String tfVarsCred = config.tf_vars ?: 'NONE'
    String destroyFlag = (params.ACTION == 'destroy') ? '-destroy' : ''
    String planName    = "tfplan-${env.BUILD_ID}.tfplan"

    def runPlan = { String varFileFlag ->
        dir(targetDir) {
            sh "terraform plan ${varFileFlag} ${destroyFlag} -out=${planName} -input=false"
            sh "terraform show -no-color ${planName} > ${planName}.txt"
            archiveArtifacts artifacts: "${planName}, ${planName}.txt", allowEmptyArchive: false
            stash name: 'tfplan', includes: planName
            env.TF_PLAN_FILE = planName
        }
    }

    if (tfVarsCred != 'NONE') {
        withCredentials([file(credentialsId: tfVarsCred, variable: 'TF_VARS_FILE')]) {
            runPlan("-var-file=${env.TF_VARS_FILE}")
        }
    } else {
        runPlan('')
    }
}
