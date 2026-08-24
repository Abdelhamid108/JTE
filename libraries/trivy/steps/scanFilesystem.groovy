// steps/scanFilesystem.groovy — Scan application source/dependencies.
//
// Contract:
//   input : application source tree (config.app_dir)
//   output: trivy-fs-report.<format> archived as a build artifact
//   fails : any finding at/above severity_threshold (blocking policy)

void call(Map args = [:]) {
    String appDir     = args.app_dir ?: config.app_dir ?: 'application'
    String severity   = config.severity_threshold ?: 'CRITICAL,HIGH'
    String exitCode   = config.exit_code ?: '1'
    String format     = config.report_format ?: 'table'
    String ignoreFile = config.ignore_file ?: ''
    String ignoreFlag = ignoreFile ? "--ignorefile ${ignoreFile}" : ''
    String reportFile = "trivy-fs-report.${format == 'table' ? 'txt' : format}"

    echo "trivy/scanFilesystem: scanning '${appDir}' (severity>=${severity}, blocking)"
    sh "trivy fs --severity ${severity} --exit-code ${exitCode} --format ${format} ${ignoreFlag} -o ${reportFile} ${appDir}"

    archiveArtifacts artifacts: reportFile, allowEmptyArchive: true
}
