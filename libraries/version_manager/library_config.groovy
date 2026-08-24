// library_config.groovy — Version Manager library configuration schema
//
// AWS auth: the S3-backed version registry is accessed using only the
// agent's ambient IRSA identity — no 'aws_credentials_id' / static key
// binding is accepted by this library.
//
// CHANGE: 'app_dir' and 'version_file' are now declared here — the
// existing readVersion.groovy referenced config.app_dir/config.version_file
// without them being part of the schema (see Step 2.2 of the plan).

fields {
    required {
    }
    optional {
        app_dir             = String   // Default: 'application'
        version_file        = String   // Default: 'VERSION'
        target_environment  = String
        repository          = String
        registry_path       = String   // s3://... path to the JSON version registry
        promotion_order     = List     // e.g. ['dev', 'test', 'prod']
        strict_promotion    = Boolean  // Default: true — gate is always enforced, no bypass
    }
}

steps {
    checkVersion
    promoteVersion
    readVersion
    registerVersion
    versionGate
}
