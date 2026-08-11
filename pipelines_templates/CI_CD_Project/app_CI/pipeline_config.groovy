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
        skip_build    = true
        install_tools = true
    }

    // ─── Docker Image Lifecycle ─────────────────────
    docker {
        image_name     = "abdelhameed208/atos-weather-app"
        registry_creds = "docker-registry-creds"
    }

    // ─── Kubernetes Manifests & Deployment ───────────
    kubernetes {
        manifests_repo_url  = "https://github.com/mostafagheta/manifest.git"
        manifests_git_creds = "GitHub-creds"
        manifests_dir       = "manifests-repo"
        image_name          = "abdelhameed208/atos-weather-app"
        kube_creds          = "kubeconfig-creds"
        deployment          = "your-app"
    }

    // ─── Version Registry (S3-backed JSON) ──────────
    version_manager {
        registry_path       = "s3://weather-app-version-registry/SourceCodeVersions.json"
        aws_credentials_id  = "aws_creds"
    }
}
