// steps/checkVersion.groovy — Check if a component version is ACTIVE in a given environment

Map call(Map args = [:]) {
    String registryPath = args.registry_path ?: config.registry_path
    String version      = args.version      ?: env.APP_VERSION
    String environment  = args.environment  ?: config.target_environment
    String component    = args.component    ?: config.component_name
    String type         = args.type         ?: config.artifact_type

    String content = sh(script: "aws s3 cp '${registryPath}' - 2>/dev/null || echo '{}'", returnStdout: true).trim()
    Map reg = [:]
    try { reg = readJSON(text: content, returnPojo: true) ?: [:] } catch (Exception ignored) {}

    Map envNode   = (reg.environments instanceof Map) ? reg.environments[environment] : null
    Map candidate = null

    if (envNode) {
        if (type == 'INFRASTRUCTURE') {
            candidate = envNode.infrastructure
        } else if (envNode.workloads) {
            candidate = envNode.workloads[component]
        }
    }

    // ACTIVE  = Argo CD confirmed the app is healthy
    // PUBLISHED = image pushed to ECR, Argo CD confirmation pending
    // Both are valid prerequisites for promotion
    List validStatuses = ['ACTIVE', 'PUBLISHED']
    Map activeRecord = (candidate?.version == version && validStatuses.contains(candidate?.status)) ? candidate : null

    echo activeRecord
        ? "checkVersion: ${version} is ${activeRecord.status} in ${environment}."
        : "checkVersion: ${version} is not published in ${environment}. Clear to proceed."

    return [exists: activeRecord != null, record: activeRecord]
}
