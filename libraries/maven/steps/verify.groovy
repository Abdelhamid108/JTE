// steps/verify.groovy — Run the Maven 'verify' phase

void call() {
    String appDir = config.app_dir      ?: '.'
    String mvnCmd = config.maven_command ?: 'mvn'

    echo "maven/verify: running '${mvnCmd} verify' in '${appDir}'"
    dir(appDir) {
        sh "${mvnCmd} -B verify"
    }
    env.STAGE_TEST_PASSED = 'true'
}
