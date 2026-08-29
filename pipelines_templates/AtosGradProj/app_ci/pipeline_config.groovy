// JTE/app_ci/pipeline_config.groovy — library wiring for the app_ci pipeline.

pipeline_template = 'app_ci/Jenkinsfile'

libraries {

    change_detection {
        application_paths = ["application/**"]
        jte_paths         = ["JTE/**"]
        base_branch       = "main"
    }

    version_manager {
        app_dir            = "application"
        version_file       = "VERSION"
        registry_path      = "s3://petclinic-platform-version-registry/version-registry.json"
        strict_promotion   = true
    }

    maven {
        app_dir       = "application"
        maven_command = "./mvnw"
    }

    sonar {
        sonar_project        = "petclinic"
        sonar_credentials_id = "sonarqube-token"
        sonar_host_url       = "http://sonarqube:9000"
        enforce_quality_gate = false
    }

    aws {
        aws_credentials_id = "aws-jenkins-assumer"
        aws_role_arn       = "arn:aws:iam::069089526123:role/JenkinsDeploymentRole"
        aws_region         = "us-east-1"
    }

    ecr {
        aws_region         = "us-east-1"
        ecr_repository     = "petclinic"
        dockerfile_path    = "application/Dockerfile"
        build_context      = "application"
    }   
    
    trivy {
        severity_threshold = "CRITICAL,HIGH"
        exit_code          = "0"
        app_dir            = "application"
        timeout            = "20m"
    }

    docker {
        install_tools      = false
        container_port     = 8080
        host_port          = 8080
        health_check_url   = "http://localhost:8080/actuator/health"
    }

    gitops {
        git_creds            = "gitops-repo-push-token"
        gitops_branch        = "main"
        values_path_template = 'gitops/workloads/${env}/values.yaml'
        git_user_name        = "jenkins-jte"
        git_user_email       = "jenkins-jte@petclinic-platform.local"
    }
}
