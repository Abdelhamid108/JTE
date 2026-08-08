void call(Map args = [:]) {
    String version          = args.version          ?: env.APP_VERSION ?: readVersion()
    String environment      = args.environment      ?: config.target_environment
    String repository       = args.repository       ?: config.repository   ?: env.GIT_URL   ?: ''
    String branch           = args.branch           ?: env.BRANCH_NAME     ?: ''
    String buildNumber      = args.build_number     ?: env.BUILD_NUMBER    ?: ''
    String registryPath     = config.registry_path  ?: 's3://my-bucket/version-registry.json'
    String awsCredentialsId = args.aws_credentials_id ?: config.aws_credentials_id

    if (!version || !environment) { 
        error "registerVersion: Both 'version' and 'environment' are required." 
    }

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

    def withAws = { Closure body ->
        if (awsCredentialsId) {
            withCredentials([aws(credentialsId: awsCredentialsId, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                body()
            }
        } else {
            body()
        }
    }

    withAws {
        String content = sh(script: "aws s3 cp '${registryPath}' -", returnStdout: true).trim()
        
        def registry = readJSON text: content
        
        registry.versions.add(record)

        String tempFile = ".version_registry_tmp.json"
        
        writeJSON file: tempFile, json: registry, pretty: 4
        
        sh "aws s3 cp '${tempFile}' '${registryPath}'"
        
        sh "rm -f '${tempFile}'"
    }

    echo "registerVersion: '${version}' registered for '${environment}'."
}