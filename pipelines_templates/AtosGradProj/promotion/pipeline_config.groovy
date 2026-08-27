// JTE/promotion/pipeline_config.groovy — library wiring for the promotion pipeline.

libraries {

    version_manager {
        app_dir            = "application"
        version_file       = "VERSION"
        registry_path      = "s3://petclinic-platform-version-registry/version-registry.json"
        promotion_order    = ["dev", "test", "prod"]
        strict_promotion   = true
    }

    release {
        git_creds  = "gitops-repo-push-token"
        tag_prefix = "v"
    }

    ecr {
        aws_region     = "us-east-1"
        ecr_repository = "petclinic"
    }

    gitops {
        git_creds            = "gitops-repo-push-token"
        gitops_branch        = "main"
        values_path_template = 'gitops/workloads/${env}/values.yaml'
        git_user_name        = "jenkins-jte"
        git_user_email       = "jenkins-jte@petclinic-platform.local"
        pr_repo_slug         = "Abdelhamid108/AtosGraduationProject"
    }
}
