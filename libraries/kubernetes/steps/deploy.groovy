// steps/deploy.groovy

void call(Map args = [:]) {
    String kubeCreds    = args.kube_creds    ?: config.kube_creds
    String namespace    = args.namespace     ?: config.namespace    ?: 'default'
    String manifestDir  = args.manifests_dir ?: config.manifests_dir ?: 'manifests-repo'
    String targetFolder = args.target_folder ?: config.target_folder ?: '.'
    String deployName   = args.deployment    ?: config.deployment
    boolean waitForRollout = config.wait_for_rollout != null ? config.wait_for_rollout : true

    if (!kubeCreds) {
        error "kubernetes.deploy requires 'kube_creds' (Jenkins credential ID for kubeconfig)"
    }

    String deployPath = (targetFolder != '.' && manifestDir != '.') ? "${manifestDir}/${targetFolder}" : ((targetFolder != '.') ? targetFolder : manifestDir)

    echo "Applying manifests from '${deployPath}' to namespace '${namespace}'..."

    withCredentials([file(credentialsId: kubeCreds, variable: 'KUBECONFIG')]) {
        sh "kubectl apply -f ${deployPath}/ -n ${namespace}"

        if (waitForRollout && deployName) {
            echo "Waiting for rollout of deployment/${deployName}..."
            sh "kubectl rollout status deployment/${deployName} -n ${namespace} --timeout=300s"
        }
    }

    echo "Deploy complete"
}
