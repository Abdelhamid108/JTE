// steps/plan.groovy
 def call (Map config = [:]) {
    // defaults to current directory if not provided
    def targetDir = config.dir ?: '.'
    def isDestroy = config.isDestroy ? "-destroy" : "" //check if the flage isDestroy - true to run with -destroy flag

    
    def planName = tfplan-${env.BUILD_ID}-${System.currentTimeMillis()}.tfplan

    println "Generating Trraform Plan ...."
    dir(targetDir){
        sh "terraform plan ${isDestroy} -out=${planName} -input=false"
        archiveArtifcats artifacts: planName, allowEmptyArchive: false
        env.TF_PLAN_FILE = planName
    }
 }