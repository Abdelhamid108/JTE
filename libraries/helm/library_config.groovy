// library_config.groovy — Helm library configuration schema

fields {
    optional {
        chart_dir    = String   // e.g. "helm/petclinic"
        release_name = String   // e.g. "petclinic"
        value_files  = List     // e.g. ["gitops/workloads/dev/values.yaml", "gitops/workloads/prod/values.yaml"]
        strict_lint  = Boolean  // default true
    }
}

steps {
    helmLint
    helmTemplate
    helmScan
}
