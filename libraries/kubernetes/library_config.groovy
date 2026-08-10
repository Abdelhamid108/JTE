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
        target_folder       = String    // Subfolder for environment manifests (e.g. 'dev', 'prod'). Default: '.'
        deployment          = String    // Deployment name for rollout status check
        wait_for_rollout    = Boolean   // Wait for rollout. Default: true

        git_user_name       = String    // Git commit author name. Default: 'jenkins-ci'
        git_user_email      = String    // Git commit author email. Default: 'jenkins@ci.local'

        ssh_creds           = String    // SSH key credential ID for Bastion host. Default: 'ansible_ssh_key'
        bastion_ip          = String    // Public IP or hostname of Bastion host
        bastion_user        = String    // SSH user for Bastion host. Default: 'ec2-user'
        master_ip           = String    // Private IP of Master node (to fetch kubeconfig if needed)
        install_tools       = Boolean   // Install agent tools (kubectl, openssh-client). Default: false
    }
}

steps {
    checkOutRemoteSCM
    updateManifest
    validateManifest
    gitPush
    createPullRequest
    k8sDeploy
    k8sInstallTools
}
