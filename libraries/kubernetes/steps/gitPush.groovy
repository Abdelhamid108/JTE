// kubernetes/steps/gitPush.groovy

void call(Map args = [:]) {
    String gitCreds     = args.git_creds      ?: config.manifests_git_creds
    String commitMsg    = args.commit_message  ?: "ci: update k8s manifests with new image tag [skip ci]"
    String manifestDir  = args.manifests_dir   ?: config.manifests_dir  ?: 'manifests-repo'
    String branch       = args.branch          ?: config.manifests_branch ?: 'main'
    String gitUserName  = config.git_user_name ?: 'jenkins-ci'
    String gitUserEmail = config.git_user_email ?: 'jenkins@ci.local'

    if (!gitCreds) {
        error "gitPush: 'git_creds' (Jenkins credential ID) is required."
    }

    dir(manifestDir) {
        withCredentials([gitUsernamePassword(credentialsId: gitCreds, gitToolName: 'Default')]) {
            sh """
                git config user.email "${gitUserEmail}"
                git config user.name "${gitUserName}"
                git add .
                git commit -m "${commitMsg}" || echo "No changes to commit"
                git push origin HEAD:${branch}
            """
        }
    }

    echo "Manifests pushed successfully."
}