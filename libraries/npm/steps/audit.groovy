// steps/audit.groovy

void call() {
    String dirPath = config.app_dir ?: '.'

    dir(dirPath) {
        echo "Running npm audit..."
        sh "npm audit || true"
    }
}
