// steps/helmTemplate.groovy — Dry-run template rendering against environment values

void call(Map args = [:]) {
    String chartDir    = args.chart_dir ?: config.chart_dir ?: 'helm/petclinic'
    String releaseName = args.release_name ?: config.release_name ?: 'petclinic'
    List valueFiles    = args.value_files ?: config.value_files ?: []

    echo "helm/helmTemplate: Testing template rendering for release '${releaseName}' in '${chartDir}'..."

    // 1. Render base chart
    echo "helm/helmTemplate: Testing base chart rendering..."
    sh "helm template ${releaseName} ${chartDir} --debug > /dev/null"

    // 2. Render against environment-specific value overrides if provided
    valueFiles.each { valFile ->
        echo "helm/helmTemplate: Validating rendering with values file: ${valFile}..."
        if (fileExists(valFile)) {
            sh "helm template ${releaseName} ${chartDir} -f ${valFile} --debug > /dev/null"
        } else {
            echo "helm/helmTemplate: Warning — values file '${valFile}' not found. Skipping."
        }
    }

    echo "helm/helmTemplate: All template rendering tests passed successfully."
}
