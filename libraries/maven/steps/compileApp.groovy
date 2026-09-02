// steps/compileApp.groovy — Compile application source code (Fail-Fast)

void call() {
    String appDir = config.app_dir      ?: pipelineConfig.libraries?.maven?.app_dir ?: '.'
    String mvnCmd = config.maven_command ?: pipelineConfig.libraries?.maven?.maven_command ?: 'mvn'

    echo "maven/compileApp: compiling source code in '${appDir}'"
    dir(appDir) {
        sh "${mvnCmd} -B compile"
    }
}
