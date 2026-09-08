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
        infracost_api_key   = String   // Jenkins Secret text credential ID
        monthly_cost_limit  = Float    // Monthly spend ceiling in USD
        infracost_soft_fail = Boolean  // Soft-fail toggle for cost limit
        github_token        = String   // Jenkins Secret text credential ID for GitHub PR commenting
        github_repo         = String   // GitHub repository slug (e.g. owner/repo)
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
    validate
    outputArtifacts
    infracost
}
