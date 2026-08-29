// steps/versionGate.groovy
//
// Policy: the gate is STRICT and ALWAYS ENFORCED by default
// (config.strict_promotion defaults to true) — there is no administrator
// bypass, because bypassing it means silently redeploying a version that
// is already recorded as promoted to that environment. Set
// strict_promotion=false only as a deliberate, explicit override.

void call(Map args = [:]) {
    String environment = args.environment ?: config.target_environment
    boolean strict = (config.strict_promotion != null) ? config.strict_promotion.toString().toBoolean() : true

    if (!environment) {
        error "versionGate: 'environment' is required."
    }

    echo "═══════════════════════════════════════════"
    echo "  VERSION GATE — Target: ${environment}"
    echo "═══════════════════════════════════════════"

    String version = readVersion()
    Map result = checkVersion(version: version, environment: environment)

    if (result.exists) {
        if (strict) {
            error "versionGate: Version '${version}' already promoted to '${environment}'. " +
                  "Pipeline terminated. Bump your version to deploy a new release."
        } else {
            echo "versionGate: WARNING — '${version}' already promoted to '${environment}', but strict_promotion=false — continuing anyway."
            return
        }
    }

    echo "  VERSION GATE PASSED — ${version} -> ${environment}"
    echo "═══════════════════════════════════════════"
}
