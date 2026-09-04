// library_config.groovy — Sonar library configuration schema

fields {
    required {
        sonar_project        = String
        sonar_credentials_id = String
    }
    optional {
        sonar_host_url               = String
        sonar_organization           = String
        app_dir                      = String
        maven_command                = String
        enforce_quality_gate         = Boolean
        quality_gate_timeout_minutes = Integer
    }
}

steps {
    scan
}
