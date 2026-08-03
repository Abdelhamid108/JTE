// steps/testApp.groovy

void call (Map args = [:]){
    String dirPath = args.app_dir ?: config.app_dir ?: '.'

    dir (dirPath){
        echo "Running Unit Tests..."
        withEnv(['CI=true']){
            sh "npm test"
        }
    }
}