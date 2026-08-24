// JTE/terraform_ci/pipeline_config.groovy — library wiring for terraform_ci.
//
// AWS auth: no static-key config field exists anywhere below. Terraform
// and the AWS CLI use the agent's ambient IRSA identity automatically.
//
// CAVEAT: block syntax follows documented JTE conventions but has not
// been validated against your installed JTE plugin version — verify
// against a live Jenkins instance before treating this as final.

libraries {

    change_detection {
        terraform_paths = ["terraform/**"]
        jte_paths        = ["JTE/**"]
        base_branch      = "main"
    }

    terraform {
        infra_dir     = "terraform/live/production"
        install_tools = true
        softFail      = false   // Checkov blocking by policy
        tf_vars       = "petclinic-tfvars"
    }
}
