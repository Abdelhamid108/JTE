// kubernetes/steps/validateManifest.groovy

void call(Map args = [:]) {
    String manifestDir  = args.manifest_dir  ?: config.manifests_dir  ?: 'manifests-repo'
    String targetFolder = args.target_folder ?: config.target_folder ?: '.'

    String checkPath = (targetFolder != '.') ? "${targetFolder}/" : '.'

    echo "Validating Kubernetes manifests syntax in '${manifestDir}/${checkPath}'..."

    dir(manifestDir) {
        sh """
            echo "--- Running kubectl client dry-run validation on ${checkPath} ---"
            if command -v kubectl >/dev/null 2>&1; then
                kubectl apply --dry-run=client -f ${checkPath}
            else
                error "validateManifest: kubectl CLI is not installed on the build agent."
            fi
        """
    }

    echo "Kubernetes manifest validation PASSED for '${checkPath}'."
}
