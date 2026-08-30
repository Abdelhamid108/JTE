// library_config.groovy — Terraform library configuration schema

fields {
    required {
        infra_dir          = String   // Path to directory containing terraform code
        target_environment = String   // e.g. 'dev', 'test', 'prod'
    }
    optional {
        repoUrl             = String
        gitCreds            = String
        terraformBranchName = String
        terraform_version   = String   // e.g. '1.10.5'
        component_name      = String   // Component name (e.g. 'eks-cluster', 'vpc')
        aws_region          = String   // e.g. 'us-east-1'
        tf_vars             = String   // Jenkins 'file' credential holding a *.tfvars file
        is_destroy          = Boolean
        install_tools       = Boolean
        softFail            = Boolean  // Checkov soft-fail toggle
    }
}

steps {
    governanceHooks
    checkoutCode
    installTools
    init
    checkov
    plan
    approval
    deploy
    destroy
    validate
}
