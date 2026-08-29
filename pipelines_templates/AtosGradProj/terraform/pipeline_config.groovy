// JTE/terraform/pipeline_config.groovy — library wiring for terraform pipeline.

pipeline_template = 'terraform/Jenkinsfile'

libraries {

    aws {
        aws_credentials_id = "petclinic-aws-credentials"
        aws_role_arn       = "arn:aws:iam::069089526123:role/JenkinsTerraformRole"
        aws_region         = "us-east-1"
        role_session_name  = "TerraformProvisioningSession"
        role_duration      = 3600
    }

    terraform {
        infra_dir          = "."
        install_tools      = true
        softFail           = true   
        tf_vars            = "petclinic-tfvars"
        target_environment = "prod"
        component_name     = "eks-cluster"
    }

    version_manager {
        app_dir            = "."
        version_file       = "INFRA_VERSION"
        registry_path      = "s3://petclinic-platform-version-registry-069089526123-us-east-1-an/version-registry.json"
        target_environment = "prod"
        component_name     = "eks-cluster"
        artifact_type      = "INFRASTRUCTURE"
    }
}
