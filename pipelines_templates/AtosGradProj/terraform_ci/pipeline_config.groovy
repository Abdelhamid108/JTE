// JTE/terraform_ci/pipeline_config.groovy — library wiring for terraform_ci.

libraries {

    change_detection {
        terraform_paths = ["terraform/**", "*.tf", "modules/**"]
        jte_paths       = ["JTE/**"]
        base_branch     = "main"
    }

    terraform {
        infra_dir     = "."
        install_tools = false
        softFail      = false   // Checkov blocking by policy
        tf_vars       = "petclinic-tfvars"
    }
}
