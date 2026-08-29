// library_config.groovy — Trivy library configuration schema

fields {
    required {
        severity_threshold = String   // e.g. 'CRITICAL,HIGH'
    }
    optional {
        exit_code     = String   // '1' blocks pipeline, '0' report-only. Default: '1'
        ignore_file   = String   // Path to .trivyignore file
        report_format = String   // 'table', 'json', 'sarif'. Default: 'table'
        app_dir       = String   // Directory to scan with scanFilesystem. Default: '.'
        timeout       = String   // Scan timeout. Default: '20m'
    }
}

steps {
    scanFilesystem
    scanImage
}
