// steps/npmLint.groovy

void call() {
    String dirPath   = config.app_dir ?: '.'
    boolean skipLint = config.skip_lint ?: false

    if (skipLint) {
        echo "Skipping Linter as per Configuration"
        return
    }

    dir(dirPath) {
        echo "Running Code Linter"
        sh "npm run lint --if-present"
    }
}