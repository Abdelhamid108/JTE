// steps/checkVersion.groovy
//
// Reads the S3 version registry and returns whether a given component version
// is currently ACTIVE in the target environment.
// Returns: [exists: boolean, record: Map|null]

Map call(Map args = [:]) {
    String registryPath = args.registry_path ?: config.registry_path
    String version      = args.version     ?: env.APP_VERSION
    String environment  = args.environment ?: config.target_environment
    String component    = args.component   ?: config.component_name ?: env.JOB_BASE_NAME
    String type         = args.type        ?: (component?.contains('cluster') ? 'INFRASTRUCTURE' : 'APPLICATION')

    if (!registryPath) { error "checkVersion: 'registry_path' must be configured." }
    if (!version)      { error "checkVersion: 'version' is required." }
    if (!environment)  { error "checkVersion: 'environment' is required." }

    String content = sh(script: "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{}'", returnStdout: true).trim()
    Map reg = [:]
    try { reg = readJSON(text: content) } catch (Exception ignored) {}

    Map envNode = reg.environments ? reg.environments[environment] : null
    Map candidate = null

    if (envNode) {
        if (type == 'INFRASTRUCTURE') {
            candidate = envNode.infrastructure
        } else if (envNode.workloads) {
            candidate = envNode.workloads[component]
        }
    }

    Map activeRecord = (candidate?.version == version && candidate?.status == 'ACTIVE') ? candidate : null

    echo activeRecord ? "checkVersion: ${version} is ACTIVE in ${environment}."
                      : "checkVersion: ${version} is not active in ${environment}. Clear to proceed."

    return [exists: activeRecord != null, record: activeRecord]
}
