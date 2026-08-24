// steps/verify.groovy — Run the Maven 'verify' phase.
//
// Used ahead of static-analysis (sonar/scan) so that compiled classes and
// coverage data exist for the scanner to analyze. Does not replace
// maven/test — verify still runs the full test lifecycle by design, so it
// must not be configured to skip tests.

void call(Map args = [:]) {
    String appDir       = args.app_dir       ?: config.app_dir       ?: 'application'
    String mavenCommand = args.maven_command ?: config.maven_command ?: './mvnw'

    echo "maven/verify: running '${mavenCommand} verify' in '${appDir}'"
    dir(appDir) {
        sh "${mavenCommand} -B verify"
    }
}
