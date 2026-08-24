// steps/packageApp.groovy — Produce the deployable artifact (jar) for Docker build.
//
// Contract:
//   input : application/ source tree, tests already executed by maven/test
//   output: application/target/*.jar consumed by the ecr/buildImage step
//   fails : any packaging failure

void call(Map args = [:]) {
    String appDir       = args.app_dir       ?: config.app_dir       ?: 'application'
    String mavenCommand = args.maven_command ?: config.maven_command ?: './mvnw'

    // Tests already ran (and were enforced) in the maven/test step. Re-running
    // them here would duplicate work, but we never skip *compilation* or the
    // quality-relevant phases — only the redundant re-execution of tests.
    boolean skipTests = (config.skip_tests_on_package != null)
        ? config.skip_tests_on_package.toString().toBoolean()
        : true

    String skipFlag = skipTests ? '-DskipTests' : ''

    echo "maven/packageApp: packaging application in '${appDir}'"
    dir(appDir) {
        sh "${mavenCommand} -B package ${skipFlag}"
    }
}
