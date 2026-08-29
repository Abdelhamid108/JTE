def call(Closure body) {

    String credentialsId = config.aws_credentials_id
    String role = config.aws_role_arn
    String region = config.aws_region

    String sessionName =
        config.role_session_name ?: "jenkins-${env.BUILD_NUMBER ?: 'session'}"

    int duration =
        config.role_duration ?: 3600

    if (!credentialsId) {
        error "aws/assumeRole: 'aws_credentials_id' is required."
    }

    if (!role) {
        error "aws/assumeRole: 'aws_role_arn' is required."
    }

    if (!region) {
        error "aws/assumeRole: 'aws_region' is required."
    }

    echo "aws/assumeRole: assuming ${role}"

    withAWS(
        credentials: credentialsId,
        role: role,
        roleSessionName: sessionName,
        duration: duration,
        region: region
    ) {
        body()
    }
}