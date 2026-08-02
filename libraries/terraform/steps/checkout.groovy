// steps/checkout.groovy
void call (){
    String repoUrl = config.repoUrl ?: 'LOCAL'
    String gitCreds = config.gitCreds ?: 'NONE'
    String branchName = config.terraformBranchName ?: 'main'

    if (repoUrl != 'LOCAL'){
        println "Checking out remote repository: ${repoUrl} (branch ${branchName})"
            if ( gitCreds != 'NONE'){
                checkout ([
                    $class: 'GitSCM',
                    branches: [[name: "*/${branchName}"]],
                    userRemoteConfigs: [[
                        url: repoUrl,
                        credentialsId: gitCreds
                    ]]
                ])
            } else{
                checkout ([
                    $class: 'GitSCM',
                    branches: [[name: "*/${branchName}"]],
                    userRemoteConfigs: [[
                        url: repoUrl
                    ]]
                ])
            }

    }  else {
        echo "No remote repo defined. Using local project workspace."
    }
}