// pipelines_templates/CI_CD_Project/app_CD/pipeline_config.groovy
// JTE Configuration for GitOps CD Pipeline

libraries {
    kubernetes {
        kube_creds       = "kubeconfig-creds"
        namespace        = "weather-app"
        manifests_dir    = "."
        deployment       = "weather-app"
        wait_for_rollout = true
    }
}
