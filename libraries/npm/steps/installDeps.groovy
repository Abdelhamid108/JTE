// steps/installDeps.groovy

void call() {
    String dirPath = config.app_dir ?: '.'

    dir(dirPath) {
        echo "Installing Node.js dependencies cleanly..."
        sh "npm ci"
    }
}