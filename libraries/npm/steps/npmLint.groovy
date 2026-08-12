// steps/npmLint.groovy

void call() {
    String dirPath   = config.app_dir ?: '.'

    dir(dirPath) {
        echo "Running Code Linter"
        sh "npm run lint --if-present"
    }
}