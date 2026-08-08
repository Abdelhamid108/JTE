// steps/buildApp.groovy

void call (Map args = [:]){
    String dirPath   = args.app_dir ?: config.app_dir ?: '.'
    boolean skipBuild = args.skip_build ?: config.skip_build ?: false

    if (skipBuild){
        echo "Skipping Build step as per Configuration"
        return
    }

    dir (dirPath){
        echo "Building Application artifact..."
        sh "npm run build --if-present"
    }
}