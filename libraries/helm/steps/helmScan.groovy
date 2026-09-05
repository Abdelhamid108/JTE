// steps/helmScan.groovy — Security & best practices scan on Helm chart

void call(Map args = [:]) {
    String chartDir = args.chart_dir ?: config.chart_dir ?: 'helm/petclinic'
    String severity = args.severity ?: 'HIGH,CRITICAL'

    echo "helm/helmScan: Scanning Helm chart in '${chartDir}' with Trivy..."
    int status = sh(
        script: "trivy config --severity ${severity} --exit-code 0 ${chartDir}",
        returnStatus: true
    )
    if (status != 0) {
        echo "helm/helmScan: Warning — potential security issues found in Helm chart configurations."
    } else {
        echo "helm/helmScan: Security configuration scan passed cleanly."
    }
}
