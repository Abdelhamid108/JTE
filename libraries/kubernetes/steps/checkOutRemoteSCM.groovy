// kubernetes/steps/checkOutRemoteSCM.groovy

void call(Map args = [:]) {
    String gitCreds  = args.git_creds     ?: config.manifests_git_creds
    String repoUrl   = args.repo_url      ?: config.manifests_repo_url
    String branch    = args.branch        ?: config.manifests_branch ?: 'main'
    String targetDir = args.manifests_dir ?: config.manifests_dir     ?: 'manifests-repo'

    if (!gitCreds || !repoUrl) {
        error "checkOutRemoteSCM: 'git_creds' and 'repo_url' are required to checkout the manifests repository."
    }

    echo "Checking out manifests repo: ${repoUrl} (branch: ${branch}) to '${targetDir}'"

    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        userRemoteConfigs: [[credentialsId: gitCreds, url: repoUrl]],
        extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: targetDir]]
    ])

    echo "Manifests repository checked out to '${targetDir}/'"
}