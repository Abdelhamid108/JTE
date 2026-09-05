# Kubernetes Library (`kubernetes`)

The `kubernetes` library provides `kubectl`-based deployment, manifest manipulation, and cluster interaction.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        manifests_repo_url  = String
        manifests_git_creds = String
        manifests_branch    = String   // default: "main"
        manifests_dir       = String   // default: "manifests-repo"
        image_name          = String
        kube_creds          = String   // Jenkins secret file credential containing kubeconfig
        namespace           = String   // default: "default"
        deployment          = String
        wait_for_rollout    = Boolean  // default: true
    }
}
```

---

## 2. Steps Reference

### `checkOutRemoteSCM`
Clones a remote manifests repository into the build agent workspace.
- **Signature**: `void call(Map args = [:])`

---

### `updateManifest`
Updates image tags across Kubernetes deployment manifests using `sed`.
- **Signature**: `void call(Map args = [:])`

---

### `validateManifest`
Executes `kubectl apply --dry-run=client` against manifests to ensure syntax and schema validity.
- **Signature**: `void call(Map args = [:])`

---

### `gitPush`
Commits and pushes updated manifests to the remote repository.
- **Signature**: `void call(Map args = [:])`

---

### `k8sDeploy`
Applies Kubernetes manifests (`kubectl apply -f`) and monitors rollout status (`kubectl rollout status`).
- **Signature**: `void call(Map args = [:])`

---

### `k8sInstallTools`
Installs the `kubectl` CLI binary onto the build agent.
- **Signature**: `void call()`\n