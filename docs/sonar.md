# SonarQube Library (`sonar`)

The `sonar` library integrates static application security testing (SAST), code quality metrics, and Quality Gate enforcement into the pipeline using the official SonarQube Maven plugin.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    required {
        sonar_project        = String   // Project Key in SonarQube
        sonar_credentials_id = String   // Jenkins Secret text credential containing token
    }
    optional {
        sonar_host_url               = String   // SonarQube server URL (e.g. "http://localhost:9000")
        sonar_organization           = String   // SonarCloud organization
        maven_command                = String   // Maven binary (default: "mvn")
        app_dir                      = String   // Application directory (default: ".")
        enforce_quality_gate         = Boolean  // Wait for Quality Gate pass/fail (default: false)
        quality_gate_timeout_minutes = Integer  // Quality gate timeout (default: 10)
    }
}
```

---

## 2. Steps Reference

### `scan`
Runs static analysis using the Maven wrapper or CLI and enforces quality gates.
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `app_dir` (String, opt): Application root.
  - `enforce_quality_gate` (Boolean, opt): Whether to wait for Quality Gate.
- **Mechanism**:
  - Wraps execution inside `withCredentials([string(credentialsId: credentials, variable: 'SONAR_TOKEN')])`.
  - Executes: `${mvnCmd} -B sonar:sonar -Dsonar.projectKey=${project} ${hostArg} -Dsonar.token=$SONAR_TOKEN ${qgArg}`.
  - Sets `env.STAGE_SONAR_PASSED = 'true'`.

> [!NOTE]
> By using `sonar:sonar` via `./mvnw`, the build agent does not require a standalone `sonar-scanner` CLI binary installed on the host.

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    sonar {
        app_dir              = "application"
        maven_command        = "./mvnw"
        sonar_project        = "petclinic"
        sonar_credentials_id = "petclinic-sonar-cred"
        sonar_host_url       = "http://localhost:9000"
        enforce_quality_gate = true
    }
}
```\n