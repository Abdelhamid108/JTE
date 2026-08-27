// JTE/terraform/pipeline_config.groovy — library wiring for terraform pipeline.

pipeline_template = 'terraform/Jenkinsfile'

libraries {

    terraform {
        infra_dir     = "."
        install_tools = false
        softFail      = false   
        tf_vars       = "petclinic-tfvars"
    }

    version_manager {
        app_dir            = "."
        version_file       = "INFRA_VERSION"
        registry_path      = "s3://petclinic-platform-version-registry/version-registry.json"
        lock_resource_name = "atos-version-registry"
    }
}
