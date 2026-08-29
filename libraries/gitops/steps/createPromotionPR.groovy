// steps/createPromotionPR.groovy — Open a promotion pull request (test/prod).
//
// Contract:
//   input : env.GITOPS_VALUES_FILE (set by updateValues), source/target env, version
//   output: a branch pushed + a pull request opened against config.gitops_branch
//   fails : missing credentials, missing pr_repo_slug, or a failed API call
//
// Test and prod GitOps changes are NEVER pushed directly — this step always
// commits to a dedicated promotion branch and opens a PR for human approval.

void call(Map args = [:]) {
    String valuesFile     = args.values_file        ?: env.GITOPS_VALUES_FILE
    String version         = args.version            ?: env.IMAGE_TAG ?: env.APP_VERSION
    String sourceEnv       = args.source_environment ?: env.SOURCE_ENVIRONMENT
    String targetEnv       = args.target_environment ?: env.GITOPS_TARGET_ENVIRONMENT
    String baseBranch      = config.gitops_branch    ?: 'main'
    String repoSlug        = config.pr_repo_slug
    String gitCreds        = config.git_creds
    String userName        = config.git_user_name  ?: 'jenkins-jte'
    String userEmail       = config.git_user_email ?: 'jenkins-jte@petclinic-platform.local'

    if (!valuesFile) { error "gitops/createPromotionPR: no values file staged. Run updateValues first." }
    if (!targetEnv)  { error "gitops/createPromotionPR: 'target_environment' is required." }
    if (!repoSlug)   { error "gitops/createPromotionPR: 'pr_repo_slug' is required." }
    if (!gitCreds)   { error "gitops/createPromotionPR: 'git_creds' is required." }

    String promotionBranch = "promotion/${targetEnv}-${version}-${env.BUILD_NUMBER}"
    String prTitle = "Promote petclinic ${version}: ${sourceEnv ?: 'source'} -> ${targetEnv}"
    String prBody = """\
Promotes version **${version}** to **${targetEnv}**.

- Source environment: ${sourceEnv ?: 'n/a'}
- Target environment: ${targetEnv}
- Build: ${env.BUILD_NUMBER}
- Commit: ${env.GIT_COMMIT ?: 'n/a'}
""".stripIndent()

    sh "git config user.name '${userName}'"
    sh "git config user.email '${userEmail}'"
    sh "git checkout -b ${promotionBranch}"
    sh "git add ${valuesFile}"
    sh "git commit -m 'chore(gitops): promote petclinic ${version} to ${targetEnv}'"

    withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
        String remote = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()
        String authedRemote = remote.replaceFirst('https://', "https://${env.GIT_USER}:${env.GIT_TOKEN}@")
        sh "git push ${authedRemote} HEAD:${promotionBranch}"

        sh """
            curl -sf -X POST \
              -H "Authorization: token \${GIT_TOKEN}" \
              -H "Accept: application/vnd.github+json" \
              https://api.github.com/repos/${repoSlug}/pulls \
              -d '{"title": ${groovy.json.JsonOutput.toJson(prTitle)}, "head": "${promotionBranch}", "base": "${baseBranch}", "body": ${groovy.json.JsonOutput.toJson(prBody)}}'
        """
    }

    echo "gitops/createPromotionPR: opened PR from ${promotionBranch} -> ${baseBranch}"
}
