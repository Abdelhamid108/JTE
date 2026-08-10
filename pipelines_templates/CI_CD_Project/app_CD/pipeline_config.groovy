// pipelines_templates/CI_CD_Project/app_CD/pipeline_config.groovy
// JTE Configuration for GitOps CD Pipeline

libraries {
    ansible {
        terraform_job_name        = 'Atos CI-CD Project/test-infra/main'
        terraform_build_selector  = 'lastSuccessful'
        inventory_file            = 'inventory.ini'
    }
    kubernetes {
        namespace        = "weather-app"
        manifests_git_creds = "GitHub-creds"
        manifests_repo_url  = "https://github.com/mostafagheta/manifest.git"
        image_name          = "abdelhameed208/atos-weather-app"
        
        manifests_dir    = "."
        deployment       = "weather-app"
        wait_for_rollout = true
        ssh_creds        = "ansible_ssh_key"
        install_tools    = true
    }
}
