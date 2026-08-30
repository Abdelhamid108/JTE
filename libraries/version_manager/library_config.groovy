// library_config.groovy — Generic Version Manager schema

fields {
    required {
        registry_path   = String   // s3://bucket/path/version-registry.json
        component_name  = String   // Name of component (e.g. 'petclinic', 'order-service')
        artifact_type   = String   // 'APPLICATION', 'INFRASTRUCTURE', 'SERVICE'
    }
    optional {
        app_dir            = String   // Subdirectory path. Default: '.'
        version_file       = String   // Default: 'VERSION'
        promotion_order    = List     // e.g. ['dev', 'test', 'prod']
        strict_promotion   = Boolean  // Default: true
        coverage_threshold = Integer  // JaCoCo instruction coverage % (e.g. 80)
        lock_resource_name = String   // Jenkins lockable resource name
        target_environment = String
    }
}

steps {
    appLifecycleHooks
    checkVersion
    promoteVersion
    readVersion
    registerVersion
    versionGate
}
