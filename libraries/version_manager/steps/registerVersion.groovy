// steps/registerVersion.groovy
//
// AWS auth: relies on the agent's ambient IRSA identity for 'aws s3 cp'.
// Concurrency: wrapped in a Jenkins 'lock' so two builds promoting at the
// same time cannot read-modify-write the same registry object and clobber
// each other's entry (requires the Lockable Resources plugin).

void call(Map args = [:]) {
    String version      = args.version ?: env.APP_VERSION ?: readVersion()
    String environment  = args.environment ?: config.target_environment 
    String type         = args.type ?: config.artifact_type 
    String component    = args.component ?: config.component_name ?: env.JOB_BASE_NAME 
    String registryPath = args.registry_path ?: config.registry_path

    if (!registryPath) error "registerVersion: 'registry_path' must be configured."

    String commitSha = env.GIT_COMMIT ?: sh(script: "git rev-parse HEAD 2>/dev/null || echo 'unknown'", returnStdout: true).trim()
    Map record = [
        type: type, component: component, version: version, commit_sha: commitSha,
        timestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC')),
        environment: environment, status: 'ACTIVE'
    ]

    echo "version_manager: registering ${type} [${component}: ${version}] for '${environment}'..."

    lock(config.lock_resource_name ?: 'jte-version-registry') {
        
        String content = sh(script: "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{}'", returnStdout: true).trim()
        Map registry = [:]
        try { registry = readJSON(text: content) } catch (Exception e) {}

        registry.environments = registry.environments ?: [:]
        def envNode = registry.environments[environment] = registry.environments[environment] ?: [:]
        registry.history = registry.history ?: []

        if (type == 'INFRASTRUCTURE') {
            envNode.infrastructure = record
        } else {
            envNode.workloads = envNode.workloads ?: [:]
            envNode.workloads[component] = record
        }

        registry.history << record 

        writeJSON file: 'registry_tmp.json', json: registry, pretty: 4
        sh "aws s3 cp registry_tmp.json '${registryPath}' && rm -f registry_tmp.json"
    }
}
