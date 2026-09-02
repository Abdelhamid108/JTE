// hooks/sonarHooks.groovy — JTE Lifecycle Hooks for SonarQube Governance

@Validate
void validateConfig() {
    if (!config.sonar_project) {
        error "sonar: 'sonar_project' is required in pipeline_config.groovy."
    }
    if (!config.sonar_credentials_id) {
        error "sonar: 'sonar_credentials_id' is required in pipeline_config.groovy."
    }
    echo "sonar [@Validate]: config OK — project='${config.sonar_project}'"
}
