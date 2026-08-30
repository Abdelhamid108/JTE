// steps/scanImage.groovy — Security vulnerability scanner for container images

void call(Map args = [:]) {
    String image      = args.image_uri ?: env.IMAGE_URI
    String severity   = config.severity_threshold
    String exitCode   = config.exit_code   ?: '1'
    String format     = config.report_format ?: 'table'
    String timeout    = config.timeout     ?: '20m'
    String reportFile = "trivy-image-report.txt"

    if (!image) { error "trivy/scanImage: 'IMAGE_URI' env var is not set. Run buildImage() first." }

    if (args.fresh_pull == true) {
        echo "trivy/scanImage: Pulling fresh image ${image} before scan..."
        sh "docker pull ${image}"
    }

    echo "trivy/scanImage: Scanning ${image} (severity>=${severity})"
    int status = sh(
        script: """
            trivy image \\
                --timeout ${timeout} \\
                --severity ${severity} \\
                --exit-code ${exitCode} \\
                -o ${reportFile} \\
                ${image}
        """,
        returnStatus: true
    )

    archiveArtifacts artifacts: reportFile, allowEmptyArchive: true

    if (status != 0) {
        error "trivy/scanImage: Vulnerabilities found exceeding threshold (${severity})."
    }

    env.STAGE_IMAGE_SCAN_PASSED = 'true'
    echo "trivy/scanImage: Scan passed cleanly."
}