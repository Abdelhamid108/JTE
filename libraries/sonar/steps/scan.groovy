// steps/scan.groovy — Run SonarQube/SonarCloud analysis and enforce the quality gate

void call() {
    String appDir     = config.app_dir      ?: '.'
    String mvnCmd     = config.maven_command ?: 'mvn'
    String project    = config.sonar_project
    String orgArg     = config.sonar_organization ? "-Dsonar.organization=${config.sonar_organization}" : ''
    String hostArg    = config.sonar_host_url     ? "-Dsonar.host.url=${config.sonar_host_url}"         : ''
    boolean enforce   = config.enforce_quality_gate?.toBoolean() ?: false
    int timeoutMins   = (config.quality_gate_timeout_minutes ?: 10) as Integer

    withCredentials([string(credentialsId: config.sonar_credentials_id, variable: 'SONAR_TOKEN')]) {
        dir(appDir) {
            sh "${mvnCmd} -B sonar:sonar -Dsonar.projectKey=${project} ${orgArg} ${hostArg} -Dsonar.token=\$SONAR_TOKEN"
        }
    }

    if (enforce) {
        echo "sonar/scan: enforce_quality_gate=true — waiting for quality gate (timeout ${timeoutMins}m)."
        timeout(time: timeoutMins, unit: 'MINUTES') {
            def qualityGate = waitForQualityGate()
            env.SONAR_QUALITY_GATE_STATUS = qualityGate.status
            if (qualityGate.status != 'OK') {
                error "sonar/scan: Quality gate failed with status '${qualityGate.status}'."
            }
        }
    } else {
        echo "sonar/scan: enforce_quality_gate=false — results are informational."
    }
}
