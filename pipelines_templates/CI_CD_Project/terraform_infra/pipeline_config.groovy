// pipeline_config.groovy — Single Branch Terraform Infrastructure Pipeline

template_sources {
    merge = true
}

pipeline_template = 'terraform_infra/Jenkinsfile'

libraries {
    terraform {
        infra_dir           = 'infrastructure'   // Directory containing Terraform code
        tf_vars             = 'aws_tfvars'         // Jenkins File Credential ID for .tfvars file
        cloud_creds         = 'aws_creds'          // Jenkins credential ID for AWS
        is_destroy          = false
        install_tools       = true
        softFail            = true
        asnible_dir         = 'ansible'
        inventory_file      = 'hosts.ini'          // Written by Terraform (local_file resource); archived for the ansible_config pipeline

    }
}
