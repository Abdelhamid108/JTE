// library_config.groovy — Version Manager library configuration schema

fields {
    required {
    }
    optional {
        target_environment  = String
        repository          = String
        registry_path       = String
        aws_credentials_id  = String
    }
}

steps {
    checkVersion
    promoteVersion
    readVersion
    registerVersion
    versionGate
}
