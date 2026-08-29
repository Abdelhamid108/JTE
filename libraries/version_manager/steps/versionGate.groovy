// steps/versionGate.groovy
//
// Version Gate Policy: Enforces that the target component version is not already
// ACTIVE in the target environment before allowing infrastructure or application promotion.

void call(Map args = [:]) {
    String environment = args.environment ?: config.target_environment ?: 'prod'
    boolean strict = (config.strict_promotion != null) ? config.strict_promotion.toString().toBoolean() : true
    String version = args.version ?: env.APP_VERSION ?: readVersion()
    String component = args.component ?: config.component_name ?: env.JOB_BASE_NAME ?: 'eks-cluster'
    String type = args.type ?: config.artifact_type ?: (component.contains('cluster') ? 'INFRASTRUCTURE' : 'APPLICATION')

    if (!environment) {
        error "versionGate: 'environment' is required."
    }

    echo "versionGate: checking ${type} [${component}@${version}] for '${environment}'..."

    Map result = checkVersion(version: version, environment: environment, component: component, type: type)

    if (result.exists) {
        if (strict) {
            error "versionGate: Version '${version}' is already ACTIVE in '${environment}'. Bump INFRA_VERSION to deploy new changes."
        } else {
            echo "versionGate: WARNING — '${version}' is already ACTIVE in '${environment}', but strict_promotion=false. Continuing."
        }
    } else {
        echo "versionGate: PASSED — '${version}' is clear to proceed for '${environment}'."
    }
}
