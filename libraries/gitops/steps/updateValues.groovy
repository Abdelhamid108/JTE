// steps/updateValues.groovy — Update only the image tag/version in a values.yaml.
//
// Contract:
//   input : environment ('dev'|'test'|'prod'), IMAGE_TAG (or an explicit version)
//   output: the target gitops/<env>/values.yaml file modified in the workspace
//           (not yet committed — see commitChanges)
//   fails : missing environment, missing version, or a missing values file
//
// Uses 'yq' to change exactly the image.tag key so unrelated values are
// never rewritten.

void call(Map args = [:]) {
    String environment = args.environment ?: error("gitops/updateValues: 'environment' is required.")
    String version      = args.version     ?: env.IMAGE_TAG ?: env.APP_VERSION

    if (!version) {
        error "gitops/updateValues: no version available (pass 'version' or set IMAGE_TAG/APP_VERSION first)."
    }

    String template = config.values_path_template ?: 'gitops/${env}/values.yaml'
    String valuesFile = template.replace('${env}', environment)

    if (!fileExists(valuesFile)) {
        error "gitops/updateValues: '${valuesFile}' does not exist."
    }

    echo "gitops/updateValues: setting image.tag=${version} in ${valuesFile}"
    sh "yq -i '.image.tag = \"${version}\"' ${valuesFile}"

    env.GITOPS_TARGET_ENVIRONMENT = environment
    env.GITOPS_VALUES_FILE        = valuesFile
}
