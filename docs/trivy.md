# Trivy Library (`trivy`)

The `trivy` library performs vulnerability and misconfiguration scanning on container images and filesystems.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        severity_threshold = String   // Severities to flag (e.g. "CRITICAL,HIGH")
        exit_code          = String   // Return code on vulnerability findings (default: "1")
        timeout            = String   // Scan timeout (default: "20m")
        app_dir            = String   // Target scan directory
        report_format      = String   // Output format: "table", "json", "sarif"
    }
}
```

---

## 2. Steps Reference

### `scanFilesystem`
Scans application source code, package manifests, and dependencies for known vulnerabilities and embedded secrets.
- **Signature**: `void call(Map args = [:])`
- **Command**: `trivy fs --timeout ${timeout} --severity ${severity} --exit-code ${exitCode} ${appDir}`.
- **Artifacts**: Generates and archives `trivy-fs-report.txt`.

---

### `scanImage`
Scans container images for OS and package vulnerabilities.
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `image_uri` (String, opt): Target container image (defaults to `env.IMAGE_URI`).
  - `fresh_pull` (Boolean, opt): Executes `docker pull` prior to scanning (useful in promotion stages).
- **Environment**: Sets `env.STAGE_IMAGE_SCAN_PASSED = 'true'`.
- **Artifacts**: Archives `trivy-image-report.txt`.

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    trivy {
        severity_threshold = "CRITICAL,HIGH"
        exit_code          = "1"
        app_dir            = "application"
        timeout            = "20m"
    }
}
```\n