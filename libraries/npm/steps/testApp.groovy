// steps/testApp.groovy

void call() {
    String dirPath = config.app_dir ?: '.'

    dir(dirPath) {
        echo "Running Unit Tests..."
        withEnv(['CI=true']) {
            sh "npm test"
        }
    }
}