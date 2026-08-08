// kubernetes/steps/gitPush.groovy

void call(Map args = [:]) {
    String gitCreds     = args.git_creds      ?: config.manifests_git_creds
    String commitMsg    = args.commit_message  ?: "ci: update k8s manifests with new image tag [skip ci]"
    String manifestDir  = args.manifests_dir   ?: config.manifests_dir  ?: 'manifests-repo'
    String targetBranch = args.target_branch   ?: config.manifests_branch ?: 'main'
    String newBranch    = args.new_branch      ?: (args.branch ?: targetBranch)
    boolean createPR    = args.create_pr != null ? args.create_pr : false
    String gitUserName  = config.git_user_name ?: 'jenkins-ci'
    String gitUserEmail = config.git_user_email ?: 'jenkins@ci.local'

    if (!gitCreds) {
        error "gitPush: 'git_creds' (Jenkins credential ID) is required."
    }

    dir(manifestDir) {
        withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
            sh """
                git config user.email "${gitUserEmail}"
                git config user.name "${gitUserName}"
                if [ "${newBranch}" != "${targetBranch}" ]; then
                    git checkout -b "${newBranch}" || git checkout "${newBranch}"
                fi
                git add .
                git commit -m "${commitMsg}" || echo "No changes to commit"
                git push origin "${newBranch}"
            """
        }
    if (createPR && newBranch != targetBranch) {
        echo "Creating Pull Request using GitHub CLI..."
    
        withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'GIT_USER', passwordVariable: 'GH_TOKEN')]) {
         sh """
            gh pr create \
                --title "${commitMsg}" \
                --body "Automated deployment PR" \
                --base "${targetBranch}" \
                --head "${newBranch}" || echo "PR already exists or creation failed."
        """
        }
    }
                
    }

    echo "Manifests pushed successfully."
}