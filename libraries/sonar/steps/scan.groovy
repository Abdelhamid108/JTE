void call(Map args = [:]) {
    // 1. Prioritize args from Jenkinsfile, fallback to JTE config
    String appDir     = args.app_dir ?: config.app_dir ?: '.'
    String projectKey = args.sonar_project ?: config.sonar_project
    String credentials= args.credentials_id ?: config.sonar_credentials_id
    String hostUrl    = args.host_url ?: config.sonar_host_url ?: ''
    String org        = args.organization ?: config.sonar_organization ?: ''
    boolean enforce   = args.enforce_quality_gate ?: config.enforce_quality_gate?.toBoolean() ?: true

    if (!projectKey) error "sonar/scan: 'sonar_project' is required."
    if (!credentials) error "sonar/scan: 'sonar_credentials_id' is required."

    List<String> scannerArgs = [
        "-Dsonar.projectKey=${projectKey}",
        "-Dsonar.sources=."
    ]
    if (hostUrl) scannerArgs << "-Dsonar.host.url=${hostUrl}"
    if (org)     scannerArgs << "-Dsonar.organization=${org}"
    if (enforce) scannerArgs << "-Dsonar.qualitygate.wait=true"

    String argString = scannerArgs.join(" ")

    echo "sonar/scan: Running universal SonarQube analysis for '${projectKey}'..."

    withCredentials([string(credentialsId: credentials, variable: 'SONAR_TOKEN')]) {
        dir(appDir) {
            sh "sonar-scanner ${argString} -Dsonar.token=\$SONAR_TOKEN"
        }
    }
}