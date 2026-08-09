// steps/deploy.groovy — Executes the Ansible playbook against the fetched Terraform inventory

void call(Map args = [:]) {
    String targetDir  = args.playbook_dir ?: config.playbook_dir ?: '.'
    String playbook    = args.playbook_file ?: config.playbook_file ?: 'site.yml'
    String inventory   = args.inventory_file ?: config.inventory_file ?: 'hosts.ini'
    String sshCreds    = args.ssh_creds ?: config.ssh_creds ?: 'NONE'
    
    String rawExtraVars = args.extra_vars ?: config.extra_vars
    String extraVars   = rawExtraVars ? "--extra-vars \"${rawExtraVars}\"" : ""

    boolean becomeFlagSet = args.become != null ? args.become.toString().toBoolean() : (config.become != null ? config.become.toString().toBoolean() : false)
    String becomeFlag  = becomeFlagSet ? "--become" : ""

    def runPlaybook = {
        dir(targetDir) {
            if (!fileExists(inventory)) {
                error "PIPELINE STOPPED: Inventory file '${inventory}' not found in ${targetDir}. Run fetchInventory() first."
            }
            sh "ansible-playbook -i ${inventory} ${playbook} ${becomeFlag} ${extraVars}"
        }
    }

    if (sshCreds && sshCreds != 'NONE') {
        withCredentials([sshUserPrivateKey(credentialsId: sshCreds, keyFileVariable: 'ANSIBLE_SSH_KEY')]) {
            withEnv(["ANSIBLE_PRIVATE_KEY_FILE=${env.ANSIBLE_SSH_KEY}", "ANSIBLE_HOST_KEY_CHECKING=False"]) {
                runPlaybook()
            }
        }
    } else {
        runPlaybook()
    }
}
