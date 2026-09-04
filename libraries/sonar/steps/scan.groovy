// steps/scan.groovy — Run SonarQube/SonarCloud analysis and enforce the quality gate

void call() {
    String appDir     = config.app_dir ?: pipelineConfig.libraries?.maven?.app_dir ?: '.'
    String mvnCmd     = config.maven_command ?: pipelineConfig.libraries?.maven?.maven_command ?: 'mvn'
    String project    = config.sonar_project
    String orgArg     = config.sonar_organization ? "-Dsonar.organization=${config.sonar_organization}" : ''
    String hostArg    = config.sonar_host_url     ? "-Dsonar.host.url=${config.sonar_host_url}"         : ''
    boolean enforce   = config.enforce_quality_gate?.toBoolean() ?: false
    String qgArg      = enforce ? "-Dsonar.qualitygate.wait=true" : ''

    echo "sonar/scan: running SonarQube analysis for project '${project}' (enforce_quality_gate=${enforce})"

    withCredentials([string(credentialsId: config.sonar_credentials_id, variable: 'SONAR_TOKEN')]) {
        dir(appDir) {
            sh "${mvnCmd} -B sonar:sonar -Dsonar.projectKey=${project} ${orgArg} ${hostArg} -Dsonar.token=\$SONAR_TOKEN ${qgArg}"
        }
    }

    env.STAGE_SONAR_PASSED = 'true'
    echo "sonar/scan: SonarQube analysis completed successfully."
}
