// steps/test.groovy — Run application unit tests

void call() {
    String appDir       = config.app_dir          ?: '.'
    String mvnCmd       = config.maven_command     ?: 'mvn'
    String reportGlob   = config.junit_report_glob ?: '**/target/surefire-reports/*.xml'

    echo "maven/test: running tests in '${appDir}'"
    try {
        dir(appDir) {
            sh "${mvnCmd} -B test"
        }
    } finally {
        junit allowEmptyResults: true, testResults: reportGlob
    }
}
