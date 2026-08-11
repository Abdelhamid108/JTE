// kubernetes/steps/checkOutRemoteSCM.groovy

void call(Map args = [:]) {
    String gitCreds  = config.manifests_git_creds
    String repoUrl   = config.manifests_repo_url
    String branch    = args.branch ?: config.manifests_branch ?: 'main'
    String targetDir = config.manifests_dir ?: 'manifests-repo'



    echo "Checking out manifests repo: ${repoUrl} (branch: ${branch}) to '${targetDir}'"

    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        userRemoteConfigs: [[credentialsId: gitCreds, url: repoUrl]],
        extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: targetDir]]
    ])

    echo "Manifests repository checked out to '${targetDir}/'"
}