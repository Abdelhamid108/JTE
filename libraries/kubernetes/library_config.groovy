
fields {
    required {
        kube_creds  = String    
        git_creds   = String
        image_name  = String   
    }
    optional {
        namespace        = String    
        manifests_dir    = String    
        deployment       = String    
        wait_for_rollout = Boolean   

    
        git_user_name    = String   
        git_user_email   = String    
    }
}

steps {
    deploy
    updateManifest
    gitPush
}
