// JTE/terraform/pipeline_config.groovy — library wiring for terraform pipeline.

pipeline_template = 'terraform/Jenkinsfile'

libraries {

    terraform {
        infra_dir     = "."
        install_tools = true
        softFail      = false   
        tf_vars       = "petclinic-tfvars"
    }

    version_manager {
        app_dir            = "."
        version_file       = "INFRA_VERSION"
        registry_path      = "s3://petclinic-platform-version-registry-069089526123-us-east-1-an/version-registry.json"
        lock_resource_name = "atos-version-registry"
    }
}
