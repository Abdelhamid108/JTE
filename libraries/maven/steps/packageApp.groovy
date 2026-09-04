// steps/packageApp.groovy — Produce the deployable artifact (jar)

void call() {
    String appDir     = config.app_dir       ?: '.'
    String mvnCmd     = config.maven_command  ?: 'mvn'
    boolean skipTests = config.skip_tests_on_package?.toBoolean() ?: true
    String skipFlag   = skipTests ? '-DskipTests' : ''

    echo "maven/packageApp: packaging in '${appDir}'"
    dir(appDir) {
        sh "${mvnCmd} -B package ${skipFlag}"
    }
}
