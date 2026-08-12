// version_manager/steps/checkVersion.groovy

Map call(Map args = [:]) {
    String version          = args.version          ?: env.APP_VERSION ?: readVersion()
    String environment      = args.environment      ?: config.target_environment
    String registryPath     = config.registry_path
    String awsCredentialsId = config.aws_credentials_id



    String awsCommand = "aws s3 cp '${registryPath}' -"
    String content = ""

    if (awsCredentialsId) {
        withCredentials([aws(credentialsId: awsCredentialsId, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
            content = sh(script: awsCommand, returnStdout: true).trim()
        }
    } else {
        content = sh(script: awsCommand, returnStdout: true).trim()
    }

    def registry = readJSON text: content
    def match = registry.versions.find { it.version == version && it.environment == environment }

    if (match) {
        echo "checkVersion: '${version}' FOUND in '${environment}'"
    } else {
        echo "checkVersion: '${version}' not found in '${environment}'. Clear to proceed."
    }

    return [exists: match != null, record: match]
}