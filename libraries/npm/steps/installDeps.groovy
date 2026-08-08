void call (Map args = [:]){
    String dirPath = args.app_dir ?: config.app_dir ?: '.'

    dir (dirPath){
        echo "Installing Node.js dependencies cleanly..."
        sh "npm ci"
    }
}