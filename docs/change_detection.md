# Change Detection Library (`change_detection`)

The `change_detection` library inspects Git commit changesets to enable path-based stage filtering in monorepos.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        base_branch       = String   // Base comparison branch (default: "main")
        application_paths = List     // Application path patterns (default: ['application/**'])
        infra_paths       = List     // Infrastructure path patterns (default: ['infra/**'])
        gitops_paths      = List     // GitOps path patterns (default: ['gitops/**', 'helm/**'])
        jte_paths         = List     // JTE configuration paths (default: ['JTE/**'])
    }
}
```

---

## 2. Steps Reference

### `changedFiles`
Computes the repository-relative list of changed files for the current build.
- **Signature**: `List<String> call()`
- **Logic**:
  - PR builds: Diffs against target branch (`git diff --name-only origin/${CHANGE_TARGET}...HEAD`).
  - Branch commits: Diffs against previous commit (`git diff --name-only HEAD~1 HEAD`).
  - First build: Treats all repository files as changed.
- **Caching**: Caches result in `env.CHANGED_FILES` to prevent repeated Git calls.

---

### `applicationChanged`
Returns `true` if any changed file matches `application_paths` or `jte_paths`.
- **Signature**: `boolean call()`

---

### `terraformChanged`
Returns `true` if any changed file matches `infra_paths`.
- **Signature**: `boolean call()`

---

### `gitopsChanged`
Returns `true` if any changed file matches `gitops_paths` (including `helm/**`).
- **Signature**: `boolean call()`

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    change_detection {
        base_branch       = "main"
        application_paths = ["application/**"]
        infra_paths       = ["infra/**"]
        gitops_paths      = ["gitops/**", "helm/**"]
    }
}
```\n