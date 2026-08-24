// JTE/promotion/pipeline_config.groovy — library wiring for the promotion pipeline.
//
// Note: 'ecr_repository' is required here too (not just in app_ci) —
// promotion independently verifies the image exists in ECR before
// touching GitOps, rather than trusting that app_ci already checked it.
//
// CAVEAT: block syntax follows documented JTE conventions but has not
// been validated against your installed JTE plugin version — verify
// against a live Jenkins instance before treating this as final.

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
        values_path_template = 'gitops/${env}/values.yaml'
        git_user_name         = "jenkins-jte"
        git_user_email        = "jenkins-jte@petclinic-platform.local"
        pr_repo_slug          = "atos-gradproj/petclinic-platform"
    }
}
