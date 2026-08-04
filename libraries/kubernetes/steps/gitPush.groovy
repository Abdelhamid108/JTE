// steps/gitPush.groovy

void call (Map args = [:]) {
    String gitCreds      = args.git_creds      ?: config.git_creds
    String commitMsg     = args.commit_message  ?: "ci: update k8s manifests with new image tag [skip ci]"
    String manifestDir   = args.manifests_dir   ?: config.manifests_dir ?: 'k8s'
    String gitUserName   = config.git_user_name  ?: 'dummy'
    String gitUserEmail  = config.git_user_email ?: 'dummy'


     if (!gitCreds) {
        error "kubernetes.gitPush requires 'git_creds' (Jenkins credential ID)"
    }
    withCredentials([GitUsernamePassword(credentialsId: "${gitCreds}", gitToolName: 'Default')]) { 
        sh """
                git config user.email "${gitUserEmail}"
                git config user.name "${gitUserName}"
                git add ${manifestDir}/
                git commit -m "${commitMsg}" || echo "No changes"
                git push origin HEAD:${currentBranch}
        """
    }

    echo "Manifests pushed successfully."

  
}