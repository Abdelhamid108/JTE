// steps/checkoutCode.groovy
//
// For the petclinic-platform monorepo, terraform_ci runs against the SAME
// checkout as the rest of the pipeline (JTE already checked out the repo
// that triggered the build). config.repoUrl is only used for the
// (uncommon) case of running Terraform against a *different* repository
// than the one that triggered the pipeline.

void call() {
    String repoUrl = config.repoUrl ?: 'LOCAL'
    String gitCreds = config.gitCreds ?: 'NONE'
    String branchName = config.terraformBranchName ?: 'main'

    if (repoUrl != 'LOCAL') {
        echo "Checking out remote repository: ${repoUrl} (branch ${branchName})"
        if (gitCreds != 'NONE') {
            checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${branchName}"]],
                userRemoteConfigs: [[
                    url: repoUrl,
                    credentialsId: gitCreds
                ]]
            ])
        } else {
            checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${branchName}"]],
                userRemoteConfigs: [[
                    url: repoUrl
                ]]
            ])
        }
    } else {
        echo "No remote repo defined. Checking out local workspace SCM..."
        checkout scm
    }
}
