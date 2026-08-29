// JTE/app_ci/pipeline_config.groovy — library wiring for app_ci.

pipeline_template = 'app_ci/Jenkinsfile'

libraries {

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
        sonar_project        = "petclinic"
        sonar_credentials_id = "sonarqube-token"
        sonar_host_url       = "http://sonarqube:9000"
        enforce_quality_gate = true
    }

    ecr {
        aws_region         = "us-east-1"
        ecr_repository     = "petclinic"
        dockerfile_path    = "application/Dockerfile"
        build_context      = "application"
    }   
    
    trivy {
        severity_threshold = "CRITICAL,HIGH"
        exit_code          = "1"
        app_dir            = "application"
        timeout            = "20m"
    }

    docker {
        install_tools      = false
        container_port     = 8080
        health_check_path  = "/actuator/health"
    }

    release {
        git_creds  = "gitops-repo-push-token"
        tag_prefix = "v"
    }
}
