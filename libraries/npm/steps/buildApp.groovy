// steps/buildApp.groovy

void call() {
    String dirPath    = config.app_dir ?: '.'
    boolean skipBuild = config.skip_build ?: false

    if (skipBuild) {
        echo "Skipping Build step as per Configuration"
        return
    }

    dir(dirPath) {
        echo "Building Application artifact..."
        sh "npm run build --if-present"
    }
}