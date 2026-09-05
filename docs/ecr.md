# ECR Library (`ecr`)

The `ecr` library handles authentication, image tag verification, and cross-tag promotions within Amazon Elastic Container Registry (ECR). It acts as the ECR adapter for the pipeline while delegating layer operations to Docker.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    required {
        aws_region   = String   // AWS Region (e.g. "us-east-1")
        ecr_registry = String   // ECR Registry URL (e.g. "069089526123.dkr.ecr.us-east-1.amazonaws.com")
        image_name   = String   // ECR Repository name (e.g. "petclinic-project/petclinitc-app")
    }
}
```

### Configuration Parameters

| Parameter | Type | Required | Description |
|:----------|:-----|:---------|:------------|
| `aws_region` | String | **Yes** | Target AWS Region where the ECR repository is hosted. |
| `ecr_registry` | String | **Yes** | Fully-qualified ECR registry URL. |
| `image_name` | String | **Yes** | Repository namespace and name in ECR. |

---

## 2. Steps Reference

### `login`
Authenticates the host Docker daemon against the AWS ECR registry using temporary credentials.
- **Signature**: `void call()`
- **Mechanism**: Executes `aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${registry}`.
- **Environment**: Sets `env.ECR_REGISTRY = registry`.

---

### `imageExists`
Verifies whether a specific image tag already exists in ECR before building or pushing, enforcing immutable tags.
- **Signature**: `boolean call(Map args = [:])`
- **Arguments**:
  - `repository` (String, opt): Defaults to `config.image_name` or `pipelineConfig.libraries.docker.image_name`.
  - `tag` (String, req): Target image tag to check (e.g. `dev-07be5b3`).
  - `region` (String, opt): Defaults to `config.aws_region`.
- **Behavior**:
  - Executes `aws ecr describe-images`.
  - If the tag exists, throws an error halting the pipeline to prevent overwriting immutable tags.
  - Correctly verifies missing tags against `ImageNotFoundException`.

---

### `retagImage`
Promotes an existing ECR image by copying its manifest from a source tag to a target tag without downloading or re-uploading image layers.
- **Signature**: `void call(Map args = [:])`
- **Arguments**:
  - `source_tag` (String, req): Existing image tag (e.g. `dev-07be5b3`).
  - `target_tag` (String, req): New promoted tag (e.g. `test-v1.0.0-rc1`).
  - `repository` (String, opt): Defaults to `config.image_name`.
  - `region` (String, opt): Defaults to `config.aws_region`.
- **Mechanism**:
  - Retrieves image manifest via `aws ecr batch-get-image`.
  - Puts the manifest under the new tag via `aws ecr put-image`.

---

## 3. Lifecycle Hooks (`ecrHooks.groovy`)

- **`@Validate`**: Verifies that `aws_region` is present in the pipeline configuration.
- **`@BeforeStep('push')`**: Auto-authenticates Docker with ECR before `push` executes via `assumeRole { login() }`.

---

## 4. Pipeline Configuration Example

```groovy
libraries {
    ecr {
        aws_region   = "us-east-1"
        ecr_registry = "069089526123.dkr.ecr.us-east-1.amazonaws.com"
        image_name   = "petclinic-project/petclinitc-app"
    }
}
```\n