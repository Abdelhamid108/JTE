// steps/versionGate.groovy

void call(Map args = [:]) {
    String environment = args.environment ?: config.target_environment



    echo "═══════════════════════════════════════════"
    echo "  VERSION GATE — Target: ${environment}"
    echo "═══════════════════════════════════════════"

    String version = readVersion()

    Map result = checkVersion(version: version, environment: environment)

    if (result.exists) {
        error "versionGate: Version '${version}' already promoted to '${environment}'. " +
              "Pipeline terminated. Bump your version to deploy a new release."
    }

    echo "  VERSION GATE PASSED — ${version} → ${environment}"
    echo "═══════════════════════════════════════════"
}
