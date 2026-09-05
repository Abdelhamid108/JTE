# Helm Library (`helm`)

The `helm` library provides automated linting, multi-environment dry-run template rendering, and security scanning for Kubernetes Helm charts.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        chart_dir    = String   // Directory containing Chart.yaml (default: "helm/petclinic")
        release_name = String   // Helm release name (default: "petclinic")
        value_files  = List     // List of environment override value files
        strict_lint  = Boolean  // Enforce --strict during linting (default: true)
    }
}
```

---

## 2. Steps Reference

### `helmLint`
Runs strict chart syntax, schema, and structure validation.
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `chart_dir` (String, opt): Chart directory path.
  - `strict` (Boolean, opt): Whether to pass `--strict`.
- **Command**: `helm lint --strict <chart_dir>`.

---

### `helmTemplate`
Dry-runs template rendering to verify that Kubernetes manifests render cleanly without errors against base and environment-specific values.
- **Signature**: `void call(Map args = [:])`
- **Behavior**:
  - Renders base chart: `helm template <release_name> <chart_dir> --debug`.
  - Iterates over each file in `value_files` (e.g. `dev/values.yaml`, `prod/values.yaml`) and tests rendering.

---

### `helmScan`
Executes security and best-practices auditing on Helm templates.
- **Signature**: `void call(Map args = [:])`
- **Command**: `trivy config --severity HIGH,CRITICAL --exit-code 0 <chart_dir>`.

---

## 3. Pipeline Configuration Example

In `pipeline_config.groovy`:

```groovy
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
```

In `Jenkinsfile`:

```groovy
stages {
    stage('Helm Lint')        { steps { helmLint() } }
    stage('Template Dry-Run') { steps { helmTemplate() } }
    stage('Security Scan')    { steps { helmScan() } }
}
```\n