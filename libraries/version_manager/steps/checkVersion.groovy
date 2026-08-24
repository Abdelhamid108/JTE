// steps/checkVersion.groovy
//
// AWS auth: relies on the agent's ambient IRSA identity for 'aws s3 cp'.
// A missing/unreadable registry is treated as a hard failure — it is
// safer to stop the pipeline than to silently assume "no versions exist".

Map call(Map args = [:]) {
    String version      = args.version     ?: env.APP_VERSION ?: readVersion()
    String environment  = args.environment ?: config.target_environment
    String registryPath = config.registry_path ?: 's3://your-bucket-name/version-registry.json'

    if (!version)     { error "checkVersion: 'version' is required." }
    if (!environment) { error "checkVersion: 'environment' is required." }

    String content
    try {
        content = sh(script: "aws s3 cp '${registryPath}' -", returnStdout: true).trim()
    } catch (Exception e) {
        error "checkVersion: could not read the version registry at '${registryPath}': ${e.message}"
    }

    def registry
    try {
        registry = readJSON text: content
    } catch (Exception e) {
        error "checkVersion: version registry at '${registryPath}' is corrupted/unparseable: ${e.message}"
    }

    def match = registry.versions.find { it.version == version && it.environment == environment }

    if (match) {
        echo "checkVersion: '${version}' FOUND in '${environment}'"
    } else {
        echo "checkVersion: '${version}' not found in '${environment}'. Clear to proceed."
    }

    return [exists: match != null, record: match]
}
