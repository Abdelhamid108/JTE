// steps/checkov.groovy

void call (){
    String targetDir = config.infra_dir ?: '.'
    String softFail = config.softFail ? '--soft-fail' : ""

    echo "Executing Checkov tests ...."
    dir(targetDir) {
        sh "checkov -d . --framework terraform ${softFail}"
    }
}