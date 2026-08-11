// kubernetes/steps/gitPush.groovy

void call(Map args = [:]) {
    String gitCreds    = config.manifests_git_creds
    String manifestDir = config.manifests_dir ?: 'manifests-repo'
    String targetBranch = args.target_branch
    String newBranch    = args.new_branch

    withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'GIT_USER', passwordVariable: 'GH_TOKEN')]) {
        sh """
            git config --global user.email "${config.git_user_email ?: 'jenkins@ci.local'}"
            git config --global user.name "${config.git_user_name ?: 'jenkins-ci'}"
            git config --global advice.addEmbeddedRepo false
            gh auth setup-git
        """

        dir(manifestDir) {
            sh """
                git checkout -b "${newBranch}" || git checkout "${newBranch}"
                git add .
                git commit -m "ci: update k8s manifests [skip ci]" || echo "No changes to commit"
                git push origin "${newBranch}"
            """

            sh """
                gh pr create \\
                    --title "ci: update k8s manifests" \\
                    --body "Automated deployment PR" \\
                    --base "${targetBranch}" \\
                    --head "${newBranch}" || echo "PR already exists or creation failed."
            """
        }
    }
}