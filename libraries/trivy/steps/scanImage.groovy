// steps/scanImage.groovy — Scan the built Docker image before it is pushed.
//
// Contract:
//   input : env.IMAGE_URI (set by ecr/buildImage) — image must already be
//           built locally on this agent
//   output: trivy-image-report.<format> archived as a build artifact
//   fails : any finding at/above severity_threshold (blocking policy) —
//           this step MUST run, and MUST pass, before ecr/pushImage.

void call(Map args = [:]) {
    String image      = args.image_uri ?: env.IMAGE_URI
    String severity   = config.severity_threshold ?: 'CRITICAL,HIGH'
    String exitCode   = config.exit_code ?: '1'
    String format     = config.report_format ?: 'table'
    String ignoreFile = config.ignore_file ?: ''
    String timeout    = config.timeout ?: '10m'

    String ignoreFlag = ignoreFile ? "--ignorefile ${ignoreFile}" : ''
    String reportFile = "trivy-image-report.${format == 'table' ? 'txt' : format}"

    if (!image) {
        error "trivy/scanImage: no image reference found. Run ecr/buildImage first, or pass image_uri."
    }

    echo "trivy/scanImage: scanning ${image} (severity>=${severity}, blocking, timeout=${timeout})"
    int status = sh(
        script: """
            trivy image \
            --timeout ${timeout} \
            --severity ${severity} \
            --exit-code ${exitCode} \
            --format ${format} \
            ${ignoreFlag} \
            -o ${reportFile} \
            ${image}
        """,
        returnStatus: true
    )

    archiveArtifacts artifacts: reportFile, allowEmptyArchive: true

    if (status != 0) {
        error "trivy/scanImage: scan failed or vulnerabilities matching ${severity} were found."
    }
}