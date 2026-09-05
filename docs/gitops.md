# GitOps Library (`gitops`)

The `gitops` library provides automation for modifying manifest values, pushing Git feature branches, and opening pull requests.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        repo_url            = String   // Manifests Git repo URL
        git_creds           = String   // Jenkins credentials ID for Git push
        base_branch         = String   // Target branch (default: "main")
        target_file         = String   // Target values file
        image_key           = String   // Key to update (default: "image.tag")
        verify_url          = String   // Verification endpoint URL
        verify_timeout_secs = Integer  // Endpoint timeout
    }
}
```

---

## 2. Steps Reference

### `updateValues`
Modifies an image tag or value key in a YAML/Helm values file using Python regex/PyYAML.
- **Signature**: `void call(Map args = [:])`
- **Arguments**: `file`, `key`, `value`.

---

### `commitChanges`
Creates a Git commit with manifest updates and pushes to a feature branch.
- **Signature**: `void call(Map args = [:])`
- **Arguments**: `branch_name`, `commit_message`.

---

### `createPromotionPR`
Opens a GitHub Pull Request using GitHub's REST API.
- **Signature**: `void call(Map args = [:])`
- **Arguments**: `title`, `body`, `head_branch`, `base_branch`.

---

### `approvalGuardrail`
Halts execution with a configurable timeout waiting for human sign-off before proceeding.
- **Signature**: `void call(Map args = [:])`

---

### `verifyHttpEndpoint`
Polls an HTTP/HTTPS URL until an HTTP 200 OK status is returned.
- **Signature**: `void call(Map args = [:])`\n