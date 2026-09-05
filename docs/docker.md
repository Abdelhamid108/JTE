# Docker Library (`docker`)

The `docker` library manages container builds, runtime health checks, registry pushes, image tagging, and cleanup.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    required {
        image_name     = String   // Container repository name
        registry_creds = String   // Jenkins credentials ID for Docker registry
    }
    optional {
        registry_url          = String   // Registry endpoint (e.g. "069089526123.dkr.ecr.us-east-1.amazonaws.com")
        dockerfile_path       = String   // Path to Dockerfile (default: "Dockerfile")
        build_context         = String   // Build context directory (default: ".")
        container_port        = Integer  // Exposed container port (default: 8080)
        health_check_path     = String   // Health endpoint (default: "/actuator/health")
        validate_wait_seconds = Integer  // Timeout for startup validation (default: 60)
    }
}
```

---

## 2. Steps Reference

### `buildImage`
Builds a Docker container image with `--pull`, tags it with the full registry path, and registers `env.IMAGE_URI`.
- **Signature**: `String call(Map args = [:])`
- **Arguments**:
  - `tag` (String, opt): Target image tag (default: `'latest'`).
  - `dockerfile` (String, opt): Custom Dockerfile path.
  - `build_context` (String, opt): Custom build directory.
- **Environment**: Sets `env.IMAGE_URI = fullImage`.

---

### `push`
Pushes image(s) to the configured registry. Supports full image URIs, multiple images, or bare tags.
- **Signature**: `void call(def images = null)`
- **Behavior**:
  - If no parameter is provided, defaults to `env.IMAGE_URI`.
  - If passed a bare tag (e.g. `dev-51eb753`), automatically prepends `${registry_url}/${image_name}:`.
  - Retries up to 3 times on transient network failures.

---

### `containerValidate`
Starts the container locally in detached mode and validates that it passes HTTP health checks before pushing.
- **Signature**: `void call(Map args = [:])`
- **Behavior**:
  - Runs container with port mapping: `docker run -d -p <host_port>:<container_port> <image>`.
  - Polls `<health_check_path>` until HTTP 200 OK or timeout.
  - Automatically stops and removes container on completion.

---

### `cleanup`
Removes local container images to conserve disk space on build agents.
- **Signature**: `void call(def images)`

---

### `promoteDockerImage`
Pulls an existing source image, re-tags it to a target image name/tag, and pushes the new image.
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `source_image` (String, req): Source image reference.
  - `target_image` (String, req): Target image reference.

---

### `dockerLogin` / `logout`
Direct username/password authentication against standard Docker registries (e.g. Docker Hub).
- **Signature**: `void dockerLogin()`, `void logout()`.

---

## 3. Lifecycle Hooks (`dockerHooks.groovy`)

- **`@Validate`**: Validates required configuration keys (`registry_url`, `image_name`).
- **`@AfterStep('buildImage')`**: Automatically triggers `containerValidate()` and `scanImage()` following every successful container build.
- **`@CleanUp`**: Cleans workspace after build finishes.

---

## 4. Pipeline Configuration Example

```groovy
libraries {
    docker {
        dockerfile_path       = "application/Dockerfile"
        build_context         = "application"
        registry_url          = "069089526123.dkr.ecr.us-east-1.amazonaws.com"
        image_name            = "petclinic-project/petclinitc-app"
        container_port        = 8080
        health_check_path     = "/actuator/health"
        validate_wait_seconds = 120
    }
}
```\n