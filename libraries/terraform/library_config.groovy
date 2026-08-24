// library_config.groovy — Terraform library configuration schema
//
// AWS authentication uses only the ambient IRSA identity available to the
// Jenkins agent pod — no 'cloud_creds' / static AWS key binding is
// accepted by this library (project architecture explicitly prohibits
// permanent IAM access keys).
//
// CHANGE (per team decision, no longer using Ansible): the previous
// 'archiveInventory' step and its 'inventory_file' field have been
// removed. Terraform in this project never generates an Ansible
// inventory, so there is nothing for that step to archive.

fields {
    required {
    }
    optional {
        repoUrl             = String
        gitCreds            = String
        terraformBranchName = String

        infra_dir           = String

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
