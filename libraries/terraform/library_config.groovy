// library_config.groovy — Terraform library configuration schema

fields {
    required {
    }
    optional {
        repoUrl             = String
        gitCreds            = String
        terraformBranchName = String

        infra_dir           = String
        terraform_version   = String   // e.g. '1.10.5'
        component_name      = String   // e.g. 'eks-cluster'
        target_environment  = String   // e.g. 'prod'
        aws_region          = String   // e.g. 'us-east-1'

        tf_vars             = String   // Jenkins 'file' credential holding a *.tfvars file

        is_destroy          = Boolean
        install_tools       = Boolean

        softFail            = Boolean  // Checkov soft-fail toggle — see steps/checkov.groovy
    }
}

steps {
    checkoutCode
    installTools
    init
    checkov
    plan
    approval
    deploy
    destroy
    governanceHooks
}
