// steps/helmLint.groovy — Lint Helm chart syntax and structure

void call(Map args = [:]) {
    String chartDir   = args.chart_dir ?: config.chart_dir ?: 'helm/petclinic'
    boolean strict    = args.strict ?: (config.strict_lint != null ? config.strict_lint.toBoolean() : true)
    String strictFlag = strict ? '--strict' : ''

    echo "helm/helmLint: Linting Helm chart in '${chartDir}' (strict=${strict})..."

    sh "helm lint ${strictFlag} ${chartDir}"
    echo "helm/helmLint: Chart linting passed cleanly for '${chartDir}'."
}
