// kubernetes/steps/validateManifest.groovy

void call(Map args = [:]) {
    String manifestDir  = config.manifests_dir ?: 'manifests-repo'
    String targetFolder = args.target_folder ?: config.target_folder ?: '.'

    String checkPath = (targetFolder != '.') ? "${targetFolder}/" : '.'

    echo "Validating Kubernetes manifests syntax in '${manifestDir}/${checkPath}'..."

    dir(manifestDir) {
        sh "kubectl apply --dry-run=client --validate=false -f ${checkPath}"
    }

    echo "Kubernetes manifest validation PASSED for '${checkPath}'."
}
