// pipeline_config.groovy — Single Branch Ansible Configuration Pipeline

template_sources {
    merge = true
}

pipeline_template = 'ansible_config/Jenkinsfile'

libraries {
    ansible {
        playbook_dir              = 'ansible'                          // Directory containing site.yml / roles
        playbook_file              = 'site.yml'
        inventory_file             = 'hosts.ini'                        // Must match terraform's inventory_file
        ssh_creds                 = 'ansible_ssh_key'                  // Jenkins SSH private key credential ID
        terraform_job_name        = 'CI_CD_Project/terraform_infra'    // Upstream job name (folder-qualified) that archived hosts.ini
        terraform_build_selector  = 'lastSuccessful'
        install_tools             = true
        become                    = true
    }
}
