// pipeline_config.groovy — CI/CD Application Release Pipeline
//
// This config wires up the npm, docker, kubernetes, and version_manager
// libraries for the branch-aware CI/CD Jenkinsfile template.
//
// Workflows:
//   PR to dev   → build, test, docker build, container validate, register PR_VALIDATED
//   Merge to dev → build, test, docker build, validate, push, update manifests, deploy DEV
//   Merge to main → promote immutable artifact to PRODUCTION, update manifests

template_sources {
    merge = true
}

pipeline_template = 'app_deployment/Jenkinsfile'

libraries {

    // ─── Application Build & Test ───────────────────
    npm {
        app_dir       = "."
        skip_lint     = false
        install_tools = true
    }

    // ─── Docker Image Lifecycle ─────────────────────
    docker {
        image_name            = "abdelhameed208/atos-weather-app"
        registry_creds        = "docker-registry-creds"
        registry_url          = "https://index.docker.io/v1/"
        docker_file_name      = "Dockerfile"
        docker_file_dir       = "."
        no_cache              = false
        validate_wait_seconds = 10
    }

    // ─── Kubernetes Manifests & Deployment ───────────
    kubernetes {
        manifests_repo_url  = "https://github.com/mostafagheta/manifest.git"
        manifests_git_creds = "github-creds"
        manifests_branch    = "main"
        manifests_dir       = "."
        image_name          = "abdelhameed208/atos-weather-app"
        kube_creds          = "kubeconfig-creds"
        namespace           = "default"
        deployment          = "your-app"
        wait_for_rollout    = true
        git_user_name       = "jenkins-ci"
        git_user_email      = "jenkins@ci.local"
    }

    // ─── Version Registry (S3-backed JSON) ──────────
    version_manager {
        registry_path    = "s3://weather-app-version-registry/SourceCodeVersions.json"
        promotion_order  = ["DEV", "PRODUCTION"]
        strict_promotion = true
    }
}
