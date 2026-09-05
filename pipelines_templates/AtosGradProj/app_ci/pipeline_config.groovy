// JTE/app_ci/pipeline_config.groovy — library wiring for app_ci.

pipeline_template = 'app_ci/Jenkinsfile'

libraries {

    aws {
        aws_credentials_id = "petclinic-aws-credentials"
        aws_role_arn       = "arn:aws:iam::069089526123:role/JenkinsTerraformRole"
        aws_region         = "us-east-1"
        role_session_name  = "PetClinicAppSession"
        role_duration      = 3600
    }

    version_manager {
        app_dir            = "application"
        version_file       = "VERSION"
        registry_path      = "s3://petclinic-platform-version-registry-069089526123-us-east-1-an/version-registry.json"
        promotion_order    = ["dev", "test", "prod"]
        strict_promotion   = true
        artifact_type      = "APPLICATION"
        component_name     = "petclinic"
        coverage_threshold = 80
    }

    maven {
        app_dir       = "application"
        maven_command = "./mvnw"
    }

    sonar {
        app_dir              = "application"
        maven_command        = "./mvnw"
        sonar_project        = "petclinic"
        sonar_credentials_id = "petclinic-sonar-cred"
        sonar_host_url       = "http://localhost:9000"
        enforce_quality_gate = true
    }

    docker {
        dockerfile_path       = "application/Dockerfile"
        build_context         = "application"
        registry_url          = "069089526123.dkr.ecr.us-east-1.amazonaws.com"
        image_name            = "petclinic-project/petclinitc-app"
        container_port        = 8080
        health_check_path     = "/actuator/health"
        validate_wait_seconds = 120
    }

    ecr {
        aws_region   = "us-east-1"
        ecr_registry = "069089526123.dkr.ecr.us-east-1.amazonaws.com"
        image_name   = "petclinic-project/petclinitc-app"
    }
    
    trivy {
        severity_threshold = "CRITICAL,HIGH"
        exit_code          = "1"
        app_dir            = "application"
        timeout            = "20m"
    }

    release {
        git_creds  = "gitops-repo-push-token"
        tag_prefix = "v"
    }
}
