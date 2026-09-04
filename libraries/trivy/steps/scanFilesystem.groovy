// steps/scanFilesystem.groovy — Scan application source/dependencies

void call() {
    String appDir     = config.app_dir       ?: '.'
    String severity   = config.severity_threshold
    String exitCode   = config.exit_code     ?: '0'
    String format     = config.report_format ?: 'table'
    String timeout    = config.timeout       ?: '20m'
    String ignoreFlag = config.ignore_file   ? "--ignorefile ${config.ignore_file}" : ''
    String reportFile = "trivy-fs-report.${format == 'table' ? 'txt' : format}"

    echo "trivy/scanFilesystem: scanning '${appDir}' (severity>=${severity})"
    int status = sh(
        script: """
            set -o pipefail
            trivy fs \\
                --timeout ${timeout} \\
                --severity ${severity} \\
                --exit-code ${exitCode} \\
                --format ${format} \\
                ${ignoreFlag} \\
                ${appDir} 2>&1 | tee ${reportFile}
        """,
        returnStatus: true
    )

    archiveArtifacts artifacts: reportFile, allowEmptyArchive: true

    if (status != 0) {
        error "trivy/scanFilesystem: Trivy failed to complete successfully."
    }

    env.STAGE_FS_SCAN_PASSED = 'true'
    echo "trivy/scanFilesystem: scan completed. Report archived as ${reportFile}."
}