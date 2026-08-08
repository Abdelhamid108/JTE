// steps/audit.groovy

void call(Map args = [:]) {
    String dirPath = args.app_dir ?: config.app_dir ?: '.'

    dir(dirPath) {
        echo "Running npm audit..."
        sh "npm audit || true"
    }
}
