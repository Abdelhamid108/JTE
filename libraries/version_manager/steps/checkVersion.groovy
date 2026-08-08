import groovy.json.JsonSlurperClassic

Map call(Map args = [:]) {
    String version          = args.version          ?: env.APP_VERSION
    String environment      = args.environment      ?: config.target_environment
    String registryPath     = config.registry_path  ?: 's3://your-bucket-name/version-registry.json'
    String awsCredentialsId = args.aws_credentials_id ?: config.aws_credentials_id

    if (!version)     { error "checkVersion: 'version' is required." }
    if (!environment) { error "checkVersion: 'environment' is required." }

    String awsCommand = "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{\"versions\":[]}'"
    String content = ""

    if (awsCredentialsId) {
        withCredentials([aws(credentialsId: awsCredentialsId, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
            content = executeShell(awsCommand)
        }
    } else {
        content = executeShell(awsCommand)
    }

    def registry = new JsonSlurperClassic().parseText(content)
    def match = registry.versions.find { it.version == version && it.environment == environment }

    if (match) {
        echo "checkVersion: '${version}' FOUND in '${environment}'"
    } else {
        echo "checkVersion: '${version}' not found in '${environment}'. Clear to proceed."
    }

    return [exists: match != null, record: match]
}

String executeShell(String command) {
    return sh(script: command, returnStdout: true).trim()
}