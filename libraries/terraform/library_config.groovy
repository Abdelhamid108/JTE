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
    validate
    checkov
    plan
    approval
    deploy
    destroy
}
