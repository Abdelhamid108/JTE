// steps/checkout.groovy
def call (Map config = [:]){
    def isRemote = config.isRemote ?: false
    def targetDir = config.dir ?: '.'

    dir (targetDir){
        if (isRemote){
            def repoUrl = config.url
            def branchName = config.branchName ?: 'main'
            def creds = config.credentialsId 

            if (!repoUrl){
                error "You must specify repository url"
            }
            
            println "Checking out remote repository: ${repoUrl} (branch ${branchName})"
            checkout ([
                $class: 'GitSCM',
                branches: [[name: "*/${branchName}"]],
                userRemoteConfigs: [[
                    url: repoUrl,
                    credentialsId: creds
                ]]
            ])
        } else{
            println "Checking out current repository code..."
            checkout scm
        }
    }
}