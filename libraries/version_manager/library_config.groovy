// library_config.groovy — Version Manager library configuration schema

fields {
    required {
    }
    optional {
        app_dir             = String   // Default: '.' or 'application'
        version_file        = String   // Default: 'VERSION' or 'INFRA_VERSION'
        target_environment  = String
        repository          = String
        registry_path       = String   // s3://... path to the JSON version registry
        promotion_order     = List     // e.g. ['dev', 'test', 'prod']
        strict_promotion    = Boolean  // Default: true — gate is always enforced, no bypass
        lock_resource_name  = String   // Jenkins lockable resource name for S3 sync
        artifact_type       = String   // e.g. 'APPLICATION', 'INFRASTRUCTURE'
        component_name      = String   // e.g. 'petclinic', 'eks-cluster'
    }
}

steps {
    checkVersion
    promoteVersion
    readVersion
    registerVersion
    versionGate
}
