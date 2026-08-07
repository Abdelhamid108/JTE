fields {
    required {
    }
    optional {
        repoUrl             = String    
        gitCreds            = String    
        terraformBranchName = String    

        infra_dir           = String    
        tf_vars             = String    

        cloud_creds         = String    

        is_destroy          = Boolean   
        install_tools       = Boolean   

        softFail            = Boolean   
    }
}

steps {
    checkoutCode
    init
    validate
    checkov
    plan
    approval
    deploy
    destroy
    terratest
}
