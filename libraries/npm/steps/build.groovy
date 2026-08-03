// steps/build.groovy

void call (Map args = [:]){
    String dirPath = args.app_dir ?: config.app_dir ?: '.'

    dir (dirPath){
        echo "Building Application artifact..."
        sh "npm run build"
    }
}