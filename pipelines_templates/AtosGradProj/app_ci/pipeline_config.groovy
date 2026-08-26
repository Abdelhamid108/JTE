// JTE/app_ci/pipeline_config.groovy — library wiring for the app_ci pipeline.
//
// Scoped to only the libraries this pipeline actually calls. No secrets
// live in this file — *_creds / *_credentials_id values are Jenkins
// credential IDs (references), never literal secrets.
//
// CAVEAT: block syntax follows documented JTE conventions but has not
// been validated against your installed JTE plugin version — verify
// against a live Jenkins instance before treating this as final.

pipeline_template = 'app_ci/Jenkinsfile'


libraries {

    change_detection {
        application_paths = ["application/**"]
        jte_paths            = ["JTE/**"]
        base_branch          = "main"
    }

    version_manager {
        app_dir       = "application"
        version_file  = "VERSION"
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
        exit_code          = "1"
        app_dir            = "application"
        timeout            = "20m"
    }

    gitops {
        git_creds            = "gitops-repo-push-token"
        gitops_branch        = "main"
        values_path_template = 'gitops/${env}/values.yaml'
        git_user_name         = "jenkins-jte"
        git_user_email        = "jenkins-jte@petclinic-platform.local"
    }
}
