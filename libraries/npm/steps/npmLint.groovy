// steps/lint.groovy
void call (Map args = [:]){
    String dirPath = args.app_dir ?: config.app_dir ?: '.'
    boolean skipLint = args.skip_lint ?: config.skip_lint ?: false

    if (skipLint){
        echo "Skipping Linter as per Configration"
        return
    }
    dir (dirPath){
        echo "Running Code Linter"
        sh "npm run lint --if-present"
    }
}