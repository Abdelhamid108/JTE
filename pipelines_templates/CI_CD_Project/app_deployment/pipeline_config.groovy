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
        app_dir   = '.'
        skip_lint = false
    }

    // ─── Docker Image Lifecycle ─────────────────────
    docker {
        image_name              = 'your-registry/your-app'    // CHANGE: full image name
        registry_creds          = 'docker-registry-creds'     // CHANGE: Jenkins credentials ID for Docker registry
        registry_url            = 'https://index.docker.io/v1/'
        docker_file_name        = 'Dockerfile'
        docker_file_dir         = '.'
        no_cache                = false
        validate_wait_seconds   = 10                          // seconds to wait during container validation
        // health_url            = 'http://localhost:3000/health' // uncomment if your app exposes a health endpoint
    }

    // ─── Kubernetes Manifests & Deployment ───────────
    kubernetes {
        manifests_repo_url  = 'https://github.com/your-org/your-k8s-manifests.git'  // CHANGE: manifests repo URL
        manifests_git_creds = 'github-creds'                  // CHANGE: Jenkins credentials ID for manifests repo
        manifests_branch    = 'main'
        manifests_dir       = 'manifests-repo'
        image_name          = 'your-registry/your-app'        // CHANGE: must match docker.image_name
        kube_creds          = 'kubeconfig-creds'              // CHANGE: Jenkins credentials ID for kubeconfig
        namespace           = 'default'
        deployment          = 'your-app'                      // CHANGE: Kubernetes deployment name
        wait_for_rollout    = true
        git_user_name       = 'jenkins-ci'
        git_user_email      = 'jenkins@ci.local'
    }

    // ─── Version Registry (S3-backed JSON) ──────────
    version_manager {
        registry_path       = 's3://your-bucket/version-registry.json'  // CHANGE: S3 path to registry file
        // aws_credentials_id = 'aws-creds'                  // uncomment if Jenkins agent doesn't have IAM role
        promotion_order     = ['DEV', 'PRODUCTION']
        strict_promotion    = true
    }
}
