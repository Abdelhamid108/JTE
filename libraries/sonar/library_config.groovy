// library_config.groovy — Sonar library configuration schema
//
// Default policy (per team decision): Sonar is a NON-BLOCKING, informational
// gate. The scan always runs, but a failed/unmet quality gate only warns —
// it does not stop the pipeline — unless 'enforce_quality_gate' is set true.

fields {
    required {
        sonar_project      = String
    }
    optional {
        sonar_host_url        = String   // Default: read from Jenkins global Sonar server config if omitted
        sonar_organization    = String
        sonar_credentials_id  = String   // Jenkins secret-text credential holding the Sonar token
        app_dir                = String   // Default: 'application'
        maven_command          = String   // Default: './mvnw'
        enforce_quality_gate  = Boolean  // Default: false (soft/non-blocking)
        quality_gate_timeout_minutes = String // Default: '10'
    }
}

steps {
    scan
}
