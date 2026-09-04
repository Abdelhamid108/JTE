// steps/registerVersion.groovy — Registers deployment state in S3 version registry

void call(Map args = [:]) {
    String registryPath = config.registry_path
    String component    = config.component_name
    String type         = config.artifact_type
    String environment  = args.environment ?: config.target_environment
    String version      = args.version     ?: env.APP_VERSION
    String status       = args.status      ?: 'ACTIVE'
    String commitSha    = env.GIT_COMMIT   ?: sh(script: "git rev-parse HEAD 2>/dev/null || echo 'unknown'", returnStdout: true).trim()
    String timestamp    = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))

    Map record = [type: type, component: component, version: version,
                  commit_sha: commitSha, timestamp: timestamp, environment: environment, status: status]

    echo "version_manager/registerVersion: ${type} ${component}@${version} -> ${status} (${environment})"

    Closure doUpdate = {
        int attempt = 0
        while (attempt < 3) {
            attempt++
            try {
                String content = sh(script: "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{}'", returnStdout: true).trim()
                Map reg = [:]
                try { reg = readJSON(text: content, returnPojo: true) ?: [:] } catch (Exception ignored) {}

                reg.environments = (reg.environments instanceof Map) ? reg.environments : [:]
                Map envNode = (reg.environments[environment] instanceof Map) ? reg.environments[environment] : [:]
                reg.history = (reg.history instanceof List) ? reg.history : []

                if (type == 'INFRASTRUCTURE') {
                    envNode.infrastructure = record
                    if (status == 'DESTROYED' && envNode.workloads) {
                        envNode.workloads.each { name, wl ->
                            reg.history << (new HashMap(wl) + [status: 'DESTROYED', timestamp: timestamp, reason: 'INFRASTRUCTURE_TEARDOWN'])
                        }
                        envNode.workloads = [:]
                    }
                } else {
                    Map workloadsMap = (envNode.workloads instanceof Map) ? envNode.workloads : [:]
                    if (status == 'DESTROYED') {
                        workloadsMap.remove(component)
                    } else {
                        workloadsMap[component] = record
                    }
                    envNode['workloads'] = workloadsMap
                }

                reg.environments[environment] = envNode
                reg.history << record
                writeJSON file: 'registry_tmp.json', json: reg, pretty: 4
                sh "aws s3 cp registry_tmp.json '${registryPath}' && rm -f registry_tmp.json"
                echo "version_manager/registerVersion: Updated S3 registry at ${registryPath}"
                return
            } catch (Exception e) {
                if (attempt >= 3) throw e
                sleep(attempt * 2)
            }
        }
    }

    if (config.lock_resource_name) {
        lock(config.lock_resource_name) { doUpdate() }
    } else {
        doUpdate()
    }
}
