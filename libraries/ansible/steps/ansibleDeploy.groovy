// steps/ansibleDeploy.groovy — Executes the Ansible playbook against the fetched Terraform inventory

void call(Map args = [:]) {
    String targetDir = config.playbook_dir ?: '.'
    String playbook  = args.playbook_file ?: config.playbook_file 
    String inventory = config.inventory_file 

    dir(targetDir) {
        if (config.ssh_creds) {
            withCredentials([sshUserPrivateKey(credentialsId: config.ssh_creds, keyFileVariable: 'ANSIBLE_SSH_KEY')]) {
                withEnv(["ANSIBLE_HOST_KEY_CHECKING=False"]) {
                    sh """
                        cp "\$ANSIBLE_SSH_KEY" ansiblekey.pem
                        chmod 400 ansiblekey.pem
                        ansible-playbook -i ${inventory} ${playbook}
                    """
                }
            }
        }
    }
}
