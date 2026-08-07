// pipeline_config.groovy — Terraform Infrastructure Pipeline
//
// This config wires up the terraform library for the infrastructure
// provisioning Jenkinsfile template.

template_sources {
    merge = true
}

pipeline_template = 'terraform_infra/Jenkinsfile'

libraries {
    terraform {
        infra_dir           = 'infrastructure'
        tf_vars             = 'terraform_tfvars'
        cloud_creds         = 'aws_creds'         // CHANGE: Jenkins credentials ID for cloud provider
        is_destroy          = false
        install_tools       = true
        softFail            = false
    }
}
