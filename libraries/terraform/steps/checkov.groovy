// steps/checkov.groovy

def call (Map config = [:]){
    def targetDir = config.dir ?: '.'
    def softFail = config.softFail ? '--soft-fail' : ""

    println "Executing Checkov tests ...."
    dir(targetDir) {
        sh "checkov -d . --framework terraform ${softFail}"
    }
}