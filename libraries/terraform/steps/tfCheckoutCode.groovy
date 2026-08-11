// steps/tfCheckoutCode.groovy

void call() {
    if (!config.repoUrl) {
        echo "No remote repo defined. Checking out local workspace SCM..."
        checkout scm
        return
    }

    String branchName = config.terraformBranchName ?: 'main'
    echo "Checking out remote repository: ${config.repoUrl} (branch ${branchName})"

    def remoteConfig = [url: config.repoUrl]
    if (config.gitCreds) {
        remoteConfig.credentialsId = config.gitCreds
    }

    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branchName}"]],
        userRemoteConfigs: [remoteConfig]
    ])
}
