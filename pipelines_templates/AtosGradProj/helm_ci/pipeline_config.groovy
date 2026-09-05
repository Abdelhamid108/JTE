// JTE/helm_ci/pipeline_config.groovy — library wiring for Helm & GitOps CI.

pipeline_template = 'helm_ci/Jenkinsfile'

libraries {

    helm {
        chart_dir    = "helm/petclinic"
        release_name = "petclinic"
        value_files  = [
            "gitops/workloads/dev/values.yaml",
            "gitops/workloads/prod/values.yaml"
        ]
        strict_lint  = true
    }
}
