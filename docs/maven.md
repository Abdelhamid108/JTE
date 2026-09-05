# Maven Library (`maven`)

The `maven` library handles Java application compilation, unit testing, packaging, and lifecycle verification.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        app_dir       = String   // Directory containing pom.xml (default: ".")
        maven_command = String   // Maven executable or wrapper (default: "mvn")
    }
}
```

---

## 2. Steps Reference

### `compileApp`
Executes compilation of Java source code to detect syntax errors early in the pipeline (fail-fast).
- **Signature**: `void call(Map args = [:])`
- **Command**: `${mvnCmd} -B compile`.

---

### `test`
Executes unit tests and collects JUnit XML reports.
- **Signature**: `void call()`
- **Command**: `${mvnCmd} -B test`.
- **Post-Action**: Calls Jenkins `junit` step to archive test results from `**/target/surefire-reports/*.xml`.

---

### `packageApp`
Compiles and packages the deployable artifact (JAR/WAR).
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `skip_tests_on_package` (Boolean, opt): Appends `-DskipTests` (default: `true`).
- **Command**: `${mvnCmd} -B package -DskipTests`.

---

### `verify`
Executes the full Maven `verify` phase for integration testing and plugin validation.
- **Signature**: `void call()`
- **Environment**: Sets `env.STAGE_TEST_PASSED = 'true'`.

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    maven {
        app_dir       = "application"
        maven_command = "./mvnw"
    }
}
```\n