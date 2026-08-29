// steps/registerVersion.groovy
//
// Reads the S3 version registry, updates ACTIVE/DESTROYED state for the
// given component, and writes it back. Retries up to 3 times with backoff
// to handle transient S3 write conflicts from concurrent pipeline runs.
//
// When infrastructure is DESTROYED, all active workloads under that
// environment are individually emitted to history with INFRASTRUCTURE_TEARDOWN
// before their active records are cleared.

void call(Map args = [:]) {
    String registryPath = args.registry_path ?: config.registry_path
    if (!registryPath) error "registerVersion: 'registry_path' must be configured."

    String version     = args.version     ?: env.APP_VERSION
    String environment = args.environment ?: config.target_environment ?: 'prod'
    String type        = args.type        ?: 'INFRASTRUCTURE'
    String component   = args.component   ?: config.component_name ?: env.JOB_BASE_NAME ?: 'eks-cluster'
    String status      = args.status      ?: 'ACTIVE'
    String commitSha   = env.GIT_COMMIT   ?: sh(script: "git rev-parse HEAD 2>/dev/null || echo 'unknown'", returnStdout: true).trim()
    String timestamp   = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))

    Map record = [type: type, component: component, version: version, commit_sha: commitSha,
                  timestamp: timestamp, environment: environment, status: status]

    echo "registerVersion: ${type} ${component}@${version} -> ${status} (${environment})"

    int attempt = 0
    while (attempt < 3) {
        attempt++
        try {
            String content = sh(script: "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{}'", returnStdout: true).trim()
            Map reg = [:]
            try { reg = readJSON(text: content) } catch (Exception ignored) {}

            reg.environments = reg.environments ?: [:]
            Map envNode = reg.environments[environment] = reg.environments[environment] ?: [:]
            reg.history = reg.history ?: []

            if (type == 'INFRASTRUCTURE') {
                envNode.infrastructure = record
                if (status == 'DESTROYED' && envNode.workloads) {
                    envNode.workloads.each { name, wl ->
                        reg.history << (new HashMap(wl) + [status: 'DESTROYED', timestamp: timestamp, reason: 'INFRASTRUCTURE_TEARDOWN'])
                    }
                    envNode.workloads = [:]
                }
            } else {
                envNode.workloads = envNode.workloads ?: [:]
                if (status == 'DESTROYED') { envNode.workloads.remove(component) }
                else                       { envNode.workloads[component] = record }
            }

            reg.history << record
            writeJSON file: 'registry_tmp.json', json: reg, pretty: 4
            sh "aws s3 cp registry_tmp.json '${registryPath}' && rm -f registry_tmp.json"
            return

        } catch (Exception e) {
            if (attempt >= 3) throw e
            sleep(attempt * 2)
        }
    }
}
