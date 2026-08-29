// steps/versionGate.groovy
//
// Version Gate Policy:
// 1. (Unless skip_active_check) Enforces that the target component version is not
//    already ACTIVE in the target environment.
// 2. Enforces the strict monotonic promotion dependency chain: dev -> test -> prod.

void call(Map args = [:]) {
    String environment      = args.environment ?: config.target_environment ?: 'prod'
    boolean strict          = (config.strict_promotion != null) ? config.strict_promotion.toString().toBoolean() : true
    boolean skipActiveCheck = args.skip_active_check ?: false
    String version          = args.version ?: env.APP_VERSION ?: readVersion()
    String component        = args.component ?: config.component_name ?: env.JOB_BASE_NAME ?: 'petclinic'
    String type             = args.type ?: config.artifact_type ?: (component.contains('cluster') ? 'INFRASTRUCTURE' : 'APPLICATION')
    List order              = config.promotion_order ?: ['dev', 'test', 'prod']

    if (!environment) {
        error "versionGate: 'environment' is required."
    }

    echo "═══════════════════════════════════════════"
    echo "  VERSION GATE — ${type} [${component}@${version}] -> '${environment}'${skipActiveCheck ? ' (active-check skipped: PR context)' : ''}"
    echo "═══════════════════════════════════════════"

    // 1. Target Environment Check: Must NOT already be ACTIVE (prevents redundant deployment)
    if (!skipActiveCheck) {
        Map targetCheck = checkVersion(version: version, environment: environment, component: component, type: type)
        if (targetCheck.exists) {
            if (strict) {
                error "versionGate: Version '${version}' is already ACTIVE in '${environment}'. Bump version to deploy new changes."
            } else {
                echo "versionGate: WARNING — '${version}' is already ACTIVE in '${environment}', but strict_promotion=false. Continuing."
            }
        }
    }

    // 2. Monotonic Promotion Dependency Chain Check — always enforced
    int envIndex = order.indexOf(environment)
    if (envIndex > 0) {
        String requiredPrevEnv = order[envIndex - 1]
        echo "versionGate: Verifying prerequisite promotion in '${requiredPrevEnv}'..."
        Map prevCheck = checkVersion(version: version, environment: requiredPrevEnv, component: component, type: type)
        if (!prevCheck.exists) {
            error "versionGate: Promotion chain violated! Version '${version}' is NOT active in prerequisite environment '${requiredPrevEnv}'. Cannot promote directly to '${environment}'."
        }
        echo "versionGate: Prerequisite verified — '${version}' is ACTIVE in '${requiredPrevEnv}'."
    }

    echo "  VERSION GATE PASSED — ${version} is clear for '${environment}'"
    echo "═══════════════════════════════════════════"
}
