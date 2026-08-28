// steps/checkVersion.groovy
//
// AWS auth: relies on the agent's ambient IRSA identity for 'aws s3 cp'.
// A missing/unreadable registry is treated as a hard failure — it is
// safer to stop the pipeline than to silently assume "no versions exist".

Map call(Map args = [:]) {
    String version      = args.version     ?: env.APP_VERSION ?: readVersion()
    String environment  = args.environment ?: config.target_environment
    String component    = args.component   ?: config.component_name ?: env.JOB_BASE_NAME
    String type         = args.type        ?: config.artifact_type ?: (component.contains('infra') || component.contains('cluster') ? 'INFRASTRUCTURE' : 'APPLICATION')
    String registryPath = args.registry_path ?: config.registry_path ?: 's3://your-bucket-name/version-registry.json'

    if (!version)     { error "checkVersion: 'version' is required." }
    if (!environment) { error "checkVersion: 'environment' is required." }

    String content = "{}"
    try {
        content = sh(script: "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{}'", returnStdout: true).trim()
    } catch (Exception e) {
        echo "checkVersion: could not read the version registry at '${registryPath}', assuming empty: ${e.message}"
    }

    Map registry = [:]
    try {
        registry = readJSON text: content
    } catch (Exception e) {
        registry = [:]
    }

    def envNode = registry.environments ? registry.environments[environment] : null
    def activeRecord = null

    if (envNode) {
        if (type == 'INFRASTRUCTURE' && envNode.infrastructure) {
            if (envNode.infrastructure.version == version && envNode.infrastructure.status == 'ACTIVE') {
                activeRecord = envNode.infrastructure
            }
        } else if (envNode.workloads && envNode.workloads[component]) {
            if (envNode.workloads[component].version == version && envNode.workloads[component].status == 'ACTIVE') {
                activeRecord = envNode.workloads[component]
            }
        }
    }

    // Fallback: check legacy 'versions' array if present
    if (!activeRecord && registry.versions) {
        activeRecord = registry.versions.find { it.version == version && it.environment == environment && (it.status == null || it.status == 'ACTIVE') }
    }

    if (activeRecord) {
        echo "checkVersion: '${version}' is currently ACTIVE in '${environment}'"
        return [exists: true, record: activeRecord]
    } else {
        echo "checkVersion: '${version}' is NOT currently active in '${environment}'. Clear to proceed."
        return [exists: false, record: null]
    }
}
