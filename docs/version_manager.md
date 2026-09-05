# Version Manager Library (`version_manager`)

The `version_manager` library provides an S3-backed JSON version registry to track and gate application promotions across environments.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    required {
        registry_path   = String   // S3 URI (e.g. "s3://my-bucket/version-registry.json")
        promotion_order = List     // e.g. ["dev", "test", "prod"]
    }
    optional {
        version_file       = String   // Version file name (default: "VERSION")
        app_dir            = String   // Path containing VERSION file
        strict_promotion   = Boolean  // Enforce sequential promotion (default: true)
        component_name     = String   // Component identifier
        artifact_type      = String   // e.g. "APPLICATION", "INFRASTRUCTURE"
        coverage_threshold = Integer  // Minimum code coverage gate
    }
}
```

---

## 2. Steps Reference

### `readVersion`
Reads the version string from the project's `VERSION` file.
- **Signature**: `String call()`
- **Environment**: Sets `env.APP_VERSION`.

---

### `checkVersion`
Checks if a version has already been registered in a specific environment.
- **Signature**: `Map call(Map args = [:])`
- **Arguments**: `version`, `environment`.
- **Returns**: `[exists: boolean, status: String]`.

---

### `registerVersion`
Appends a new deployment record to the S3 JSON registry containing version, commit SHA, build number, timestamp, and environment.
- **Signature**: `void call(Map args = [:])`

---

### `versionGate`
Halts the pipeline if the version already exists in the target environment to prevent duplicate deployments.
- **Signature**: `void call(Map args = [:])`

---

### `promoteVersion`
Verifies that the version has successfully passed the prerequisite environment before registering it in the target environment.
- **Signature**: `void call(Map args = [:])`

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    version_manager {
        app_dir          = "application"
        version_file     = "VERSION"
        registry_path    = "s3://petclinic-platform-version-registry-069089526123-us-east-1-an/version-registry.json"
        promotion_order  = ["dev", "test", "prod"]
        strict_promotion = true
        component_name   = "petclinic"
        artifact_type    = "APPLICATION"
    }
}
```\n