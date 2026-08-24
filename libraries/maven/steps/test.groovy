// steps/test.groovy — Run application unit tests via the Maven wrapper.
//
// Contract:
//   input : application/ source tree (config.app_dir)
//   output: JUnit test reports published to Jenkins
//   fails : any test failure, or a non-zero mvnw exit code

void call(Map args = [:]) {
    String appDir        = args.app_dir        ?: config.app_dir        ?: 'application'
    String mavenCommand  = args.maven_command  ?: config.maven_command  ?: './mvnw'
    String reportGlob    = config.junit_report_glob ?: '**/target/surefire-reports/*.xml'

    echo "maven/test: running unit tests in '${appDir}' with '${mavenCommand} test'"

    try {
        dir(appDir) {
            sh "${mavenCommand} -B test"
        }
    } finally {
        // Publish whatever reports exist even if the tests failed, so the
        // failure is visible in Jenkins rather than only in the console log.
        junit allowEmptyResults: true, testResults: "${appDir}/${reportGlob}".replace('application/application/', 'application/')
    }
}
