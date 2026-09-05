# AWS Library (`aws`)

The `aws` library provides integration with Amazon Web Services by assuming AWS IAM roles dynamically via AWS Security Token Service (STS). It wraps Jenkins pipeline steps using the Jenkins AWS Steps plugin (`withAWS`).

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    required {
        aws_credentials_id = String   // Jenkins AWS credential ID (Access Key / Secret Key)
        aws_role_arn       = String   // Target IAM Role ARN to assume
        aws_region         = String   // Target AWS region (e.g. "us-east-1")
    }
    optional {
        role_session_name  = String   // Name of the assumed role session
        role_duration      = Integer  // Duration of STS credentials in seconds (default: 3600)
    }
}
```

### Configuration Parameters

| Parameter | Type | Required | Default | Description |
|:----------|:-----|:---------|:--------|:------------|
| `aws_credentials_id` | String | **Yes** | — | The Jenkins Credentials Store ID of the IAM user credentials used to request role assumption. |
| `aws_role_arn` | String | **Yes** | — | The ARN of the IAM role to assume (e.g. `arn:aws:iam::069089526123:role/JenkinsTerraformRole`). |
| `aws_region` | String | **Yes** | — | AWS Region for the STS API call and injected environment (e.g. `us-east-1`). |
| `role_session_name` | String | No | `"jenkins-${BUILD_NUMBER}"` | Identifier for the assumed role session in AWS CloudTrail audit logs. |
| `role_duration` | Integer | No | `3600` (1 hour) | Expiration window of the temporary STS credentials (in seconds). |

---

## 2. Steps Reference

### `assumeRole`

Wraps a closure of pipeline steps inside a temporary AWS STS assumed-role session.

- **Signature**: `void call(Closure body)`
- **Mechanism**: Calls Jenkins pipeline `withAWS(...) { body() }`.

#### Injected Environment Variables:
While inside the `assumeRole { ... }` block, the following environment variables are injected into all sub-processes (`sh`, CLI, SDKs):
- `AWS_ACCESS_KEY_ID`: Temporary STS Access Key ID.
- `AWS_SECRET_ACCESS_KEY`: Temporary STS Secret Access Key.
- `AWS_SESSION_TOKEN`: Temporary STS Security Token.
- `AWS_REGION` / `AWS_DEFAULT_REGION`: Configured region.

#### Scoping Rule:
> [!IMPORTANT]
> The injected STS credentials exist **only while the closure is executing**. As soon as the block exits, Jenkins tears down the environment variables. Any AWS CLI command (e.g. `aws ecr describe-images`, `terraform plan`) requiring AWS credentials must be executed inside `assumeRole { ... }`.

---

## 3. Pipeline Configuration Example

In `pipeline_config.groovy`:

```groovy
libraries {
    aws {
        aws_credentials_id = "petclinic-aws-credentials"
        aws_role_arn       = "arn:aws:iam::069089526123:role/JenkinsTerraformRole"
        aws_region         = "us-east-1"
        role_session_name  = "JenkinsPipelineSession"
        role_duration      = 3600
    }
}
```

In `Jenkinsfile`:

```groovy
stage('Terraform Plan') {
    steps {
        assumeRole {
            init()
            plan()
        }
    }
}
```
