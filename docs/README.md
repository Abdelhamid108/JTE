# JTE Libraries Documentation Index

Welcome to the dedicated documentation directory for all **15 Jenkins Templating Engine (JTE) libraries**.

Each document below provides the complete configuration schema (`library_config.groovy`), detailed step signatures, parameter tables, execution mechanics, and real-world configuration examples.

---

## Library Index by Category

### 1. Cloud & Container Registries
- [AWS Library (`aws.md`)](./aws.md): AWS STS role assumption (`assumeRole`) and session management.
- [ECR Library (`ecr.md`)](./ecr.md): AWS ECR authentication, tag existence verification, and manifest promotion.
- [Docker Library (`docker.md`)](./docker.md): Container build, health check validation, registry push, and image lifecycle.

### 2. Kubernetes & GitOps
- [Helm Library (`helm.md`)](./helm.md): Helm linting, multi-environment dry-run rendering, and chart security audits.
- [GitOps Library (`gitops.md`)](./gitops.md): Values modification, feature branch commits, PR creation, and endpoint health polling.
- [Kubernetes Library (`kubernetes.md`)](./kubernetes.md): Direct `kubectl` deployment, manifest updates, and rollout monitoring.

### 3. Application Build & Quality
- [Maven Library (`maven.md`)](./maven.md): Java compile, unit test, package, and verification.
- [SonarQube Library (`sonar.md`)](./sonar.md): Static code analysis, token authentication, and Quality Gate enforcement via Maven plugin.
- [Trivy Library (`trivy.md`)](./trivy.md): Container image and filesystem vulnerability scanning.
- [NPM Library (`npm.md`)](./npm.md): Node.js build, test, lint, and runtime dependency installation.

### 4. Infrastructure as Code & Configuration
- [Terraform Library (`terraform.md`)](./terraform.md): Infrastructure provisioning with Checkov SAST and speculative plans.
- [Ansible Library (`ansible.md`)](./ansible.md): Configuration management consuming Terraform build inventory artifacts.

### 5. Release & Governance
- [Version Manager Library (`version_manager.md`)](./version_manager.md): S3-backed JSON version registry and promotion gates.
- [Release Library (`release.md`)](./release.md): Git SHA resolution, version validation, and automated Git tagging.
- [Change Detection Library (`change_detection.md`)](./change_detection.md): Monorepo path diffing and selective stage execution.\n