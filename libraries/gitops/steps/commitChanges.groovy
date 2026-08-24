// steps/commitChanges.groovy — Commit and push a direct GitOps update (dev only).
//
// Contract:
//   input : env.GITOPS_VALUES_FILE (set by updateValues), a version to describe
//   output: a commit pushed to config.gitops_branch
//   fails : nothing staged, missing credentials, or a rejected push
//
// This step performs a DIRECT push and must only be used for the 'dev'
// environment. Test/prod changes go through createPromotionPR instead —
// callers (the pipeline templates) are responsible for enforcing that.

void call(Map args = [:]) {
    String valuesFile  = args.values_file ?: env.GITOPS_VALUES_FILE
    String version     = args.version     ?: env.IMAGE_TAG ?: env.APP_VERSION
    String environment = args.environment ?: env.GITOPS_TARGET_ENVIRONMENT ?: 'dev'
    String branch      = config.gitops_branch ?: 'main'
    String userName    = config.git_user_name  ?: 'jenkins-jte'
    String userEmail   = config.git_user_email ?: 'jenkins-jte@petclinic-platform.local'
    String gitCreds    = config.git_creds

    if (!valuesFile) { error "gitops/commitChanges: no values file to commit. Run updateValues first." }
    if (!gitCreds)    { error "gitops/commitChanges: 'git_creds' is required." }

    String message = "chore(gitops): deploy petclinic ${version} to ${environment}"

    sh "git config user.name '${userName}'"
    sh "git config user.email '${userEmail}'"
    sh "git add ${valuesFile}"

    int changes = sh(script: "git diff --cached --quiet", returnStatus: true)
    if (changes == 0) {
        echo "gitops/commitChanges: no staged changes for ${valuesFile}, nothing to commit."
        return
    }

    sh "git commit -m '${message}'"

    withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
        String remote = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()
        String authedRemote = remote.replaceFirst('https://', "https://${env.GIT_USER}:${env.GIT_TOKEN}@")
        sh "git push ${authedRemote} HEAD:${branch}"
    }

    echo "gitops/commitChanges: pushed '${message}' to ${branch}"
}
