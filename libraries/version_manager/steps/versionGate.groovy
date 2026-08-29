// steps/versionGate.groovy — Enforce monotonic promotion and validate release activeness

void call(Map args = [:]) {
    String environment = args.environment ?: config.target_environment
    String component   = config.component_name
    String type        = config.artifact_type
    String version     = env.APP_VERSION ?: readVersion()
    List   order       = config.promotion_order ?: []
    boolean strict     = config.strict_promotion?.toBoolean() ?: true
    boolean isPR       = env.CHANGE_ID != null

    if (!environment) { error "versionGate: 'environment' is required." }

    echo "═══════════════════════════════════════════"
    echo "  VERSION GATE — ${type} [${component}@${version}] -> '${environment}'"
    echo "═══════════════════════════════════════════"

    if (!isPR) {
        if (checkVersion(version: version, environment: environment).exists) {
            if (strict) {
                error "versionGate: '${version}' is already ACTIVE in '${environment}'. Bump version to deploy."
            } else {
                echo "versionGate: WARNING — '${version}' already ACTIVE in '${environment}'."
            }
        }
    }

    int envIndex = order.indexOf(environment)
    if (envIndex > 0) {
        String prevEnv = order[envIndex - 1]
        echo "versionGate: Verifying prerequisite '${prevEnv}'..."
        if (!checkVersion(version: version, environment: prevEnv).exists) {
            error "versionGate: Chain violated — '${version}' is NOT active in '${prevEnv}'."
        }
        echo "versionGate: Prerequisite '${prevEnv}' verified."
    }

    echo "  VERSION GATE PASSED — ${version} clear for '${environment}'"
    echo "═══════════════════════════════════════════"
}
