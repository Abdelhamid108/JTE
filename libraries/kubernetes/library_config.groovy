// library_config.groovy — Kubernetes library configuration schema

fields {
    required {
        manifests_git_creds = String    // Jenkins credential ID for manifests repo auth
        manifests_repo_url  = String    // Git URL of the Kubernetes manifests repository
        image_name          = String    // Full Docker image name (used by updateManifest)
    }
    optional {
        kube_creds          = String    // Jenkins credential ID for kubeconfig (used by deploy)
        namespace           = String    // Kubernetes namespace. Default: 'default'
        manifests_dir       = String    // Directory inside manifests repo. Default: 'manifests-repo'
        manifests_branch    = String    // Branch of manifests repo. Default: 'main'
        deployment          = String    // Deployment name for rollout status check
        wait_for_rollout    = Boolean   // Wait for rollout. Default: true

        git_user_name       = String    // Git commit author name. Default: 'jenkins-ci'
        git_user_email      = String    // Git commit author email. Default: 'jenkins@ci.local'
    }
}

steps {
    checkOutRemoteSCM
    updateManifest
    gitPush
    deploy
}
