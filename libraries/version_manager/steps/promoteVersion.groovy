// steps/promoteVersion.groovy

void call(Map args = [:]) {
    String version   = args.version ?: env.APP_VERSION ?: readVersion()
    String targetEnv = args.target_environment ?: config.target_environment

    if (!version || !targetEnv) { 
        error "promoteVersion: Both 'version' and 'target_environment' are required." 
    }

    if (checkVersion(version: version, environment: targetEnv).exists) {
        error "promoteVersion: Version '${version}' is already in '${targetEnv}'. Halting to prevent redundant deployment."
    }

    registerVersion(version: version, environment: targetEnv)
    echo "promoteVersion: Successfully promoted version '${version}' to '${targetEnv}'!"
}