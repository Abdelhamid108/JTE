# NPM Library (`npm`)

The `npm` library manages Node.js dependency installation, testing, linting, application bundling, and runtime tool provisioning.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        app_dir       = String   // Directory containing package.json (default: ".")
        install_tools = Boolean  // Install CLI tools (Docker, AWS, kubectl) at runtime
    }
}
```

---

## 2. Steps Reference

### `installDeps`
Installs npm package dependencies.
- **Signature**: `void call()`
- **Command**: `npm install` (or `npm ci`).

---

### `npmLint`
Runs the project code linter.
- **Signature**: `void call()`
- **Command**: `npm run lint`.

---

### `testApp`
Executes the test suite.
- **Signature**: `void call()`
- **Command**: `npm test`.

---

### `buildApp`
Compiles frontend or backend application code.
- **Signature**: `void call()`
- **Command**: `npm run build`.

---

### `audit`
Executes dependency security vulnerability auditing.
- **Signature**: `void call()`
- **Command**: `npm audit --audit-level=high`.

---

### `npmInstallTools`
Installs agent CLI dependencies (AWS CLI, kubectl, Docker CLI) dynamically during the build.
- **Signature**: `void call()`

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    npm {
        app_dir       = "."
        install_tools = true
    }
}
```\n