// steps/deploy.groovy — Executes the Ansible playbook against the fetched Terraform inventory

void call() {
    String targetDir  = config.playbook_dir ?: '.'
    String playbook    = config.playbook_file ?: 'site.yml'
    String inventory   = config.inventory_file ?: 'hosts.ini'
    String sshCreds    = config.ssh_creds ?: 'NONE'
    String extraVars   = config.extra_vars ? "--extra-vars \"${config.extra_vars}\"" : ""
    boolean becomeFlagSet = config.become != null ? config.become.toString().toBoolean() : false
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
