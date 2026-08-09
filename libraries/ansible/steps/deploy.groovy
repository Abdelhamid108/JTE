// steps/deploy.groovy — Executes the Ansible playbook against the fetched Terraform inventory

void call(Map args = [:]) {
    String targetDir  = args.playbook_dir ?: config.playbook_dir ?: '.'
    String playbook   = args.playbook_file ?: config.playbook_file ?: 'site.yml'
    String inventory  = args.inventory_file ?: config.inventory_file ?: 'inventory.ini'
    String sshCreds   = args.ssh_creds ?: config.ssh_creds ?: 'NONE'

    dir(targetDir) {
        if (!fileExists(inventory)) {
            error "PIPELINE STOPPED: Inventory file '${inventory}' not found in ${targetDir}. Run fetchInventory() first."
        }

        if (sshCreds && sshCreds != 'NONE') {
            withCredentials([sshUserPrivateKey(credentialsId: sshCreds, keyFileVariable: 'ANSIBLE_SSH_KEY')]) {
                withEnv(["ANSIBLE_HOST_KEY_CHECKING=False"]) {
                    sh """
                        cp "\$ANSIBLE_SSH_KEY" ansiblekey.pem
                        chmod 400 ansiblekey.pem
                        ssh -i ./ansiblekey.pem -o StrictHostKeyChecking=no ec2-user@44.213.106.223
                        ansible-playbook -i ${inventory} ${playbook}
                    """
                }
            }
        }
    }
}
