# Jenkins Templating Engine (JTE) — CI/CD Pipeline Framework

A production-grade, modular CI/CD pipeline framework built on the [Jenkins Templating Engine](https://plugins.jenkins.io/templating-engine/). This repository provides reusable JTE libraries and pipeline templates for application deployment and infrastructure provisioning, following enterprise best practices: **immutable artifacts**, **version-controlled releases**, and **strict promotion workflows**.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Repository Structure](#repository-structure)
- [Pipeline Workflows](#pipeline-workflows)
  - [Application Deployment Pipeline](#application-deployment-pipeline)
  - [Terraform Infrastructure Pipeline](#terraform-infrastructure-pipeline)
- [Libraries Reference](#libraries-reference)
  - [npm](#npm-library)
  - [docker](#docker-library)
  - [kubernetes](#kubernetes-library)
  - [version_manager](#version_manager-library)
  - [terraform](#terraform-library)
  - [ansible](#ansible-library)
- [Version Management](#version-management)
- [Version Registry (S3)](#version-registry-s3)
- [Configuration Guide](#configuration-guide)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)

---

## Architecture Overview

The framework is designed around three separate repositories:

| Repository | Purpose | Branching |
|:-----------|:--------|:----------|
| **Source Code** | Application source, Dockerfile, tests, `VERSION` file | `dev` → `main` |
| **Kubernetes Manifests** | Deployment manifests, Helm values, environment configs | `dev`, `main` |
| **Infrastructure** | Terraform modules, Ansible playbooks | `main` (single branch) |

```
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Source Code     │     │  K8s Manifests   │     │  Infrastructure  │
│  Repository      │     │  Repository      │     │  Repository      │
│                  │     │                  │     │                  │
│  • App Code      │     │  • YAML Manifests│     │  • Terraform     │
│  • Dockerfile    │     │  • Helm Values   │     │  • Ansible       │
│  • Tests         │     │  • Env Configs   │     │  • Modules       │
│  • VERSION       │     │                  │     │                  │
└────────┬─────────┘     └────────┬─────────┘     └────────┬─────────┘
         │                        │                        │
         └────────────────────────┴────────────────────────┘
                                  │
                    ┌─────────────┴──────────────┐
                    │   Jenkins + JTE Framework  │
                    │                            │
                    │   • Reusable Libraries     │
                    │   • Pipeline Templates     │
                    │   • Version Registry (S3)  │
                    └────────────────────────────┘
```

### Core Principles

- **Immutable Artifacts**: Docker images are built once and promoted through environments. Production uses the exact same image tested in DEV.
- **Version File as Source of Truth**: The `VERSION` file drives Docker image tags, registry entries, and deployment manifest tags. Never use Git commit SHAs as deployment tags.
- **Strict Promotion**: Versions must pass through each environment in order (e.g., `DEV` → `PRODUCTION`). Skipping environments is blocked.
- **Separation of Concerns**: CI (build/test/push) is decoupled from CD (manifest update/deploy). Pipeline steps have single responsibilities.

---

## Repository Structure

```
JTE/
├── README.md
│
├── libraries/                                    # Reusable JTE Libraries
│   │
│   ├── npm/                                      # Node.js build & test
│   │   ├── library_config.groovy
│   │   └── steps/
│   │       ├── installDeps.groovy
│   │       ├── build.groovy
│   │       ├── lint.groovy
│   │       ├── testApp.groovy
│   │       └── audit.groovy
│   │
│   ├── docker/                                   # Docker image lifecycle
│   │   ├── library_config.groovy
│   │   └── steps/
│   │       ├── login.groovy
│   │       ├── build.groovy
│   │       ├── tag.groovy
│   │       ├── push.groovy
│   │       ├── containerValidate.groovy
│   │       ├── cleanup.groovy
│   │       ├── promote.groovy
│   │       ├── logout.groovy
│   │       ├── composeUp.groovy
│   │       └── composeDown.groovy
│   │
│   ├── kubernetes/                               # Manifest updates & deployment
│   │   ├── library_config.groovy
│   │   └── steps/
│   │       ├── checkOutRemoteSCM.groovy
│   │       ├── updateManifest.groovy
│   │       ├── gitPush.groovy
│   │       └── deploy.groovy
│   │
│   ├── version_manager/                          # Version registry (S3-backed)
│   │   ├── library_config.groovy
│   │   └── steps/
│   │       ├── readVersion.groovy
│   │       ├── checkVersion.groovy
│   │       ├── registerVersion.groovy
│   │       ├── versionGate.groovy
│   │       └── promoteVersion.groovy
│   │
│   ├── terraform/                                # Infrastructure provisioning
│   │   ├── library_config.groovy
│   │   └── steps/
│   │       ├── checkoutCode.groovy
│   │       ├── init.groovy
│   │       ├── validate.groovy
│   │       ├── checkov.groovy
│   │       ├── plan.groovy
│   │       ├── approval.groovy
│   │       ├── deploy.groovy
│   │       ├── destroy.groovy
│   │       └── terratest.groovy
│   │
│   └── ansible/                                  # Configuration management
│       └── library_config.groovy
│
└── pipelines_templates/                          # Pipeline Templates
    └── CI_CD_Project/
        ├── app_deployment/                       # Application CI/CD
        │   ├── Jenkinsfile
        │   └── pipeline_config.groovy
        └── terraform_infra/                      # Infrastructure IaC
            ├── Jenkinsfile
            └── pipeline_config.groovy
```

---

## Pipeline Workflows

### Application Deployment Pipeline

The application deployment pipeline is **branch-aware** and implements three distinct workflows using a single `Jenkinsfile`.

#### Workflow 1 — Pull Request to `dev`

> **Purpose**: Validate new code before merging. No artifacts leave the build agent.

```
Checkout → Install Agent Dependencies → Read Version → Version Gate
→ Build & Test (npm) → Docker Build → Container Validation
→ Register Version (PR_VALIDATED)
```

| What Happens | What Does NOT Happen |
|:-------------|:---------------------|
| ✅ Version uniqueness check | ❌ No Docker login |
| ✅ npm install, lint, test, build | ❌ No image tagging |
| ✅ Docker image built locally | ❌ No image push |
| ✅ Container runtime validation | ❌ No manifest update |
| ✅ Registry updated: `PR_VALIDATED` | ❌ No deployment |

#### Workflow 2 — Merge to `dev`

> **Purpose**: Full CI/CD. Build the immutable artifact, push it, update manifests, and deploy to DEV.

```
Checkout → Install Agent Dependencies → Read Version → Version Gate
→ Build & Test (npm) → Docker Build → Container Validation
→ Login → Tag → Push → Cleanup
→ Register Version (DEV)
→ Checkout Manifests Repo → Update Image Tag (sed) → Git Push Manifests
```

#### Workflow 3 — Merge `dev` into `main`

> **Purpose**: Promote the already-tested, immutable artifact to Production. Zero rebuilds.

```
Checkout → Read Version → Promote Version (PRODUCTION)
→ Checkout Manifests Repo → Update Image Tag (sed) → Git Push Manifests
```

> **⚠️ IMPORTANT**: This workflow does **NOT** rebuild, retest, or re-push the Docker image. It reuses the exact immutable artifact that was built and pushed during Workflow 2.

---

### Terraform Infrastructure Pipeline

The infrastructure pipeline uses a **single branch** (`main`) strategy with parameterized environment selection.

#### Parameters

| Parameter | Options | Description |
|:----------|:--------|:------------|
| `TARGET_ENVIRONMENT` | `dev`, `prod` | Selects which environment to plan/apply against |
| `ACTION` | `apply`, `destroy` | Whether to create or tear down infrastructure |

#### Workflow

```
Checkout Code → Install Dependencies (Checkov)
→ Init → Validate → Checkov Security Scan
→ Plan → Approval Guardrail → Deploy / Destroy
```

| Trigger | Behavior |
|:--------|:---------|
| **Pull Request** | Runs `init` → `validate` → `checkov` → `plan`. Does NOT apply. |
| **Merge to `main`** | Full pipeline with manual **Approval Guardrail** before `deploy` or `destroy`. |

---

## Libraries Reference

### npm Library

Application build and test steps for Node.js projects.

| Step | Description |
|:-----|:------------|
| `installDeps()` | Runs `npm install` |
| `installTools()` | Installs agent dependencies (AWS CLI, kubectl CLI, Docker CLI, Git) |
| `npm.buildApp()` | Runs `npm run build` |
| `lint()` | Runs the project linter |
| `testApp()` | Runs `npm test` |
| `audit()` | Runs `npm audit` |

**Configuration**:
```groovy
npm {
    app_dir       = '.'        // Directory containing package.json
    skip_lint     = false      // Set true to skip linting
    install_tools = true       // Set true to install agent dependencies (aws-cli, kubectl, etc.)
}
```

---

### docker Library

Full Docker image lifecycle management.

| Step | Description |
|:-----|:------------|
| `login()` | Authenticates to Docker registry |
| `docker.buildImage()` | Builds Docker image, returns image reference |
| `tag()` | Tags image with version, branch, and optional release tags |
| `push(tags)` | Pushes list of tagged images to registry |
| `containerValidate()` | Runs container briefly to verify it starts without crashing |
| `cleanup(images)` | Removes local Docker images |
| `promote()` | Pulls, re-tags, and pushes an image to a new tag |
| `logout()` | Logs out of Docker registry |
| `composeUp()` | Runs `docker compose up -d` |
| `composeDown()` | Runs `docker compose down -v` |

**Configuration**:
```groovy
docker {
    image_name            = 'your-registry/your-app'   // Required: full image name
    registry_creds        = 'docker-creds-id'          // Required: Jenkins credentials ID
    registry_url          = 'https://index.docker.io/v1/'
    docker_file_name      = 'Dockerfile'
    docker_file_dir       = '.'
    no_cache              = false
    validate_wait_seconds = 10       // Seconds to wait during container validation
    // health_url          = 'http://localhost:3000/health'  // Optional health endpoint
}
```

---

### kubernetes Library

Kubernetes manifest updates and deployment via `sed` + `git push`.

| Step | Description |
|:-----|:------------|
| `checkOutRemoteSCM()` | Clones the manifests repository into `manifests-repo/` |
| `updateManifest()` | Uses `sed` to update image tags in all YAML files |
| `gitPush()` | Commits and pushes manifest changes back to the repo |
| `deploy()` | Applies manifests via `kubectl apply` and waits for rollout |

**Configuration**:
```groovy
kubernetes {
    manifests_repo_url  = 'https://github.com/org/manifests.git'  // Required
    manifests_git_creds = 'github-creds'                          // Required
    image_name          = 'your-registry/your-app'                // Required
    manifests_branch    = 'main'
    manifests_dir       = '.'
    kube_creds          = 'kubeconfig-creds'
    namespace           = 'default'
    deployment          = 'your-app'
    wait_for_rollout    = true
    git_user_name       = 'jenkins-ci'
    git_user_email      = 'jenkins@ci.local'
}
```

---

### version_manager Library

Manages version lifecycle with an S3-backed JSON registry. Supports optional AWS credentials or falls back to IAM role authentication.

| Step | Description |
|:-----|:------------|
| `readVersion()` | Reads version from `VERSION` file, sets `env.APP_VERSION` |
| `checkVersion()` | Checks if a version already exists in the registry for a given environment |
| `registerVersion()` | Adds a new version record to the S3 JSON registry |
| `versionGate()` | Blocks the pipeline if the version is already deployed to the target environment |
| `promoteVersion()` | Validates promotion path and registers version in the next environment |

**Configuration**:
```groovy
version_manager {
    registry_path      = 's3://your-bucket/version-registry.json'  // S3 path
    // aws_credentials_id = 'aws-creds'   // Uncomment if agent has no IAM role
    promotion_order    = ['DEV', 'PRODUCTION']
    strict_promotion   = true
}
```

---

### terraform Library

Terraform infrastructure provisioning with security scanning and approval gates.

| Step | Description |
|:-----|:------------|
| `checkoutCode()` | Checks out the infrastructure repository |
| `init()` | Runs `terraform init -reconfigure` |
| `validate()` | Runs `terraform fmt -check` and `terraform validate` |
| `checkov()` | Runs Checkov security scan against Terraform code |
| `plan()` | Generates and archives a Terraform plan |
| `approval()` | Manual approval gate before apply/destroy |
| `deploy()` | Applies the archived Terraform plan |
| `destroy()` | Applies a destroy plan |

**Configuration**:
```groovy
terraform {
    infra_dir     = 'infrastructure'    // Directory containing .tf files
    tf_vars       = 'aws_tfvars'        // Jenkins file credential ID for .tfvars
    cloud_creds   = 'aws_creds'         // Jenkins credential ID for cloud provider
    is_destroy    = false
    install_tools = true                // Install Checkov at runtime
    softFail      = false               // Checkov soft-fail mode
}
```

---

### ansible Library

> 🚧 **Placeholder** — Configuration management library. Steps to be implemented.

---

## Version Management

### VERSION File

Every application source repository must contain a `VERSION` file at its root:

```
1.0.0
```

This version is used as:
- **Docker Image Tag** (`your-registry/your-app:1.0.0`)
- **Version Registry Entry** (tracked in S3 JSON)
- **Deployment Version** (referenced in Kubernetes manifests)

> Git commit SHAs are stored as metadata only — never used as deployment image tags.

### Version Lifecycle

```
Developer bumps VERSION
        │
        ▼
   PR_VALIDATED  ──(PR merged to dev)──▶  DEV  ──(dev merged to main)──▶  PRODUCTION
```

---

## Version Registry (S3)

The version registry is a JSON file stored in Amazon S3. It tracks every version's progression through environments.

### Initial Registry File

Upload this to your S3 bucket to bootstrap the registry:

```json
{
    "versions": []
}
```

### Example After Pipeline Runs

```json
{
    "versions": [
        {
            "version": "1.0.0",
            "repository": "https://github.com/org/app.git",
            "branch": "dev",
            "commit_sha": "abc1234",
            "build_number": "42",
            "timestamp": "2026-08-08T01:00:00Z",
            "environment": "DEV",
            "status": "PROMOTED"
        },
        {
            "version": "1.0.0",
            "repository": "https://github.com/org/app.git",
            "branch": "main",
            "commit_sha": "abc1234",
            "build_number": "43",
            "timestamp": "2026-08-08T02:00:00Z",
            "environment": "PRODUCTION",
            "status": "PROMOTED"
        }
    ]
}
```

### AWS Authentication

The version_manager library supports two modes:

1. **IAM Role (default)**: If the Jenkins agent already has AWS credentials via an IAM instance profile or task role, no additional configuration is needed.
2. **Explicit Credentials**: Set `aws_credentials_id` in the pipeline config to use Jenkins-stored AWS credentials via `withCredentials`.

---

## Configuration Guide

### Setting Up a New Application Project

1. **Create a `pipeline_config.groovy`** in your application repository root, pointing to the app deployment template:

```groovy
template_sources {
    merge = true
}

pipeline_template = 'app_deployment/Jenkinsfile'

libraries {
    npm {
        app_dir = '.'
    }
    docker {
        image_name     = 'your-registry/your-app'
        registry_creds = 'your-docker-creds-id'
    }
    kubernetes {
        manifests_repo_url  = 'https://github.com/your-org/your-manifests.git'
        manifests_git_creds = 'your-git-creds-id'
        image_name          = 'your-registry/your-app'
    }
    version_manager {
        registry_path   = 's3://your-bucket/version-registry.json'
        promotion_order = ['DEV', 'PRODUCTION']
    }
}
```

2. **Create a `VERSION` file** in your application repository root:
```
1.0.0
```

3. **Create the S3 registry file**:
```bash
echo '{"versions":[]}' > version-registry.json
aws s3 cp version-registry.json s3://your-bucket/version-registry.json
```

4. **Configure Jenkins** with the required credentials (Docker registry, GitHub, AWS, kubeconfig).

---

## Prerequisites

### Jenkins Plugins

| Plugin | Purpose |
|:-------|:--------|
| [Jenkins Templating Engine](https://plugins.jenkins.io/templating-engine/) | Core JTE framework |
| [Pipeline: Multibranch](https://plugins.jenkins.io/workflow-multibranch/) | Branch-aware pipeline support |
| [Git](https://plugins.jenkins.io/git/) | Git SCM integration |
| [Credentials Binding](https://plugins.jenkins.io/credentials-binding/) | Secret management |
| [Docker Pipeline](https://plugins.jenkins.io/docker-workflow/) | Docker agent support |
| [Pipeline Utility Steps](https://plugins.jenkins.io/pipeline-utility-steps/) | Optional (Native Groovy `JsonSlurperClassic` is now used for JSON handling) |
| [AWS Credentials](https://plugins.jenkins.io/aws-credentials/) | AWS credential binding (`aws()`) |

### Jenkins Credentials

| Credential ID | Type | Purpose |
|:-------------|:-----|:--------|
| Docker registry creds | Username/Password | Docker login/push |
| GitHub creds | Username/Password | Clone & push manifests repo |
| AWS creds | AWS Credentials | S3 registry access, Terraform |
| Kubeconfig | Secret File | Kubernetes deployment |
| TF vars | Secret File | Terraform `.tfvars` file |

### Infrastructure

- **Amazon S3 Bucket**: For the version registry JSON file
- **Docker Registry**: Docker Hub, ECR, or any OCI-compatible registry
- **Kubernetes Cluster**: Target deployment environment
- **AWS CLI**: Installed on Jenkins agents (for S3 operations)

---

## Getting Started

1. **Clone this repository** and configure it as a JTE [Governance Tier](https://plugins.jenkins.io/templating-engine/) library source in Jenkins.

2. **Create a Multibranch Pipeline** job in Jenkins pointing to your application source code repository.

3. **Add the `pipeline_config.groovy`** to your application repository with the correct library configurations.

4. **Add a `VERSION` file** to your application repository.

5. **Bootstrap the S3 version registry**:
   ```bash
   echo '{"versions":[]}' | aws s3 cp - s3://your-bucket/version-registry.json
   ```

6. **Open a Pull Request** to `dev` — the pipeline will automatically validate the version and run CI.

7. **Merge to `dev`** — the pipeline builds, pushes the immutable image, and updates DEV manifests.

8. **Merge `dev` to `main`** — the pipeline promotes the artifact to PRODUCTION without rebuilding.
