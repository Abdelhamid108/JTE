// library_config.groovy — Trivy library configuration schema
//
// Default policy (per team decision): Trivy is a HARD/BLOCKING security
// gate. Any finding at or above 'severity_threshold' fails the pipeline.

fields {
    required {
    }
    optional {
        severity_threshold = String   // Default: 'CRITICAL,HIGH'
        exit_code            = String   // Default: '1' (non-zero => Trivy fails the shell step => pipeline stops)
        ignore_file          = String   // Optional path to a .trivyignore file
        report_format        = String   // Default: 'table' (also supports 'json', 'sarif')
        app_dir               = String   // Default: 'application' — used by scanFilesystem
        timeout               = String   // Default: '20m' — Trivy scan timeout
    }
}

steps {
    scanFilesystem
    scanImage
}
