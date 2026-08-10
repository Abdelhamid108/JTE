// library_config.groovy — Ansible library configuration schema

fields {
    required {
    }
    optional {
        // Source checkout (ansible playbooks repo — may be the same repo as terraform)
        repoUrl              = String    // Git URL of the repo containing the playbooks. Default: checkout local workspace SCM
        gitCreds             = String    // Jenkins credentials ID for git auth
        ansibleBranchName    = String    // Branch to checkout. Default: 'main'

        // Playbook location & execution
        playbook_dir         = String    // Directory containing the playbook(s). Default: '.'
        playbook_file         = String    // Entry-point playbook. Default: 'site.yml'
        extra_vars           = String    // Extra vars string passed via --extra-vars
        become               = Boolean   // true = run with --become (privilege escalation). Default: false

        // Inventory (produced by the terraform library's archiveInventory() step)
        inventory_file             = String    // Inventory filename to fetch/use. Default: 'inventory.ini'
        terraform_job_name         = String    // Upstream Jenkins job name that archived the inventory artifact
        terraform_build_selector   = String    // Copy Artifact build selector: 'lastSuccessful' (default) or a specific build number

        // Connection
        ssh_creds            = String    // Jenkins SSH private key credential ID used to connect to target hosts

        install_tools        = Boolean   // true = install ansible + ansible-lint on the agent. Default: false
    }
}

steps {
    ansibleCheckoutCode
    ansibleInstallTools
    fetchInventory
    ansibleLint
    ansibleDeploy
}
