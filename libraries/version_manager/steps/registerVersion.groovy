// version_manager/steps/registerVersion.groovy

void call(Map args = [:]) {
    String version          = args.version          ?: env.APP_VERSION ?: readVersion()
    String environment      = args.environment      ?: config.target_environment
    String repository       = args.repository       ?: config.repository   ?: env.GIT_URL   ?: ''
    String branch           = args.branch           ?: env.BRANCH_NAME     ?: ''
    String buildNumber      = args.build_number     ?: env.BUILD_NUMBER    ?: ''
    String registryPath     = config.registry_path
    String awsCredentialsId = config.aws_credentials_id



    String commitSha = env.GIT_COMMIT ?: sh(script: "git rev-parse HEAD 2>/dev/null || echo 'unknown'", returnStdout: true).trim()
    String timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))

    Map record = [
        version      : version,
        repository   : repository,
        branch       : branch,
        commit_sha   : commitSha,
        build_number : buildNumber,
        timestamp    : timestamp,
        environment  : environment,
        status       : 'PROMOTED'
    ]

    String tempFile = ".version_registry_tmp.json"
    String cpFrom = "aws s3 cp '${registryPath}' -"
    String cpTo = "aws s3 cp '${tempFile}' '${registryPath}'"

    def updateRegistry = {
        String content = sh(script: cpFrom, returnStdout: true).trim()
        def registry = readJSON text: content
        registry.versions.add(record)
        writeJSON file: tempFile, json: registry, pretty: 4
        sh cpTo
        sh "rm -f '${tempFile}'"
    }

    if (awsCredentialsId) {
        withCredentials([aws(credentialsId: awsCredentialsId, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
            updateRegistry()
        }
    } else {
        updateRegistry()
    }

    echo "registerVersion: '${version}' registered for '${environment}'."
}