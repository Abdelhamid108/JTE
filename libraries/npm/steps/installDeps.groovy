void call (Map args = [:]){
    String dirPath = args.app_dir ?: config.app_dir ?: '.'

    dir (dirPath){
        echo "Installing Node.js dependancies cleanly..."
        sh "npm ci"
    }
}