// steps/packageApp.groovy — Produce the deployable artifact (jar)

void call(Map args = [:]) {
    String appDir     = args.app_dir       ?: config.app_dir   
    String mvnCmd     = args.maven_command  ?: config.maven_command  ?: 'mvn'
    boolean skipTests = args.skip_tests_on_package?.toBoolean() ?: true
    String skipFlag   = skipTests ? '-DskipTests' : ''

    echo "maven/packageApp: packaging in '${appDir}'"
    dir(appDir) {
        sh "${mvnCmd} -B package ${skipFlag}"
    }
}
