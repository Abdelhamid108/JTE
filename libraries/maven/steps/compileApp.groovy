// steps/compileApp.groovy — Compile application source code (Fail-Fast)

void call(Map args = [:]) {
    String appDir = args.app_dir      ?: config.app_dir   
    String mvnCmd = args.maven_command ?: config.maven_command ?: 'mvn'

    echo "maven/compileApp: compiling source code in '${appDir}'"S
    dir(appDir) {
        sh "${mvnCmd} -B compile"
    }
}
