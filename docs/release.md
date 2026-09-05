# Release Library (`release`)

The `release` library manages semantic version resolution, branch/PR metadata extraction, and Git release tagging.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        git_creds        = String   // Jenkins credentials ID for Git push
        tag_prefix       = String   // Prefix for created tags (default: "v")
        version_pattern  = String   // Regex for version validation
        release_branch   = String   // Default release branch (default: "main")
    }
}
```

---

## 2. Steps Reference

### `getGitVersion`
Resolves version metadata and short commit SHA from Jenkins native environment variables.
- **Signature**: `void call(Map args = [:])`
- **Output Variables**:
  - `env.GIT_SHORT_SHA`: 7-character Git commit SHA.
  - `env.GIT_TAG`: Evaluated version (e.g. `TAG_NAME` on tags, `pr-<ID>-<SHA>` on PRs, or commit SHA on branches).

---

### `createTag`
Creates an annotated Git release tag and pushes it to origin.
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `tag_name` (String, req): Name of the Git tag to create.
  - `message` (String, opt): Tag annotation message.

---

### `validateVersion`
Validates that a version string strictly matches Semantic Versioning (`vMAJOR.MINOR.PATCH`).
- **Signature**: `boolean call(String version)`

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    release {
        git_creds  = "gitops-repo-push-token"
        tag_prefix = "v"
    }
}
```\n