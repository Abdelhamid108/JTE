// steps/lint.groovy — Syntax-checks and lints the playbook before it is run against real hosts

void call() {
    String targetDir  = config.playbook_dir ?: '.'
    String playbook    = config.playbook_file ?: 'site.yml'
    String inventory   = config.inventory_file ?: 'hosts.ini'

    dir(targetDir) {
        if (!fileExists(inventory)) {
            error "PIPELINE STOPPED: Inventory file '${inventory}' not found in ${targetDir}. Run fetchInventory() before lint()."
        }
        echo "Running ansible-playbook syntax check..."
        sh "ansible-playbook -i ${inventory} ${playbook} --syntax-check"

        echo "Running ansible-lint..."
        sh "ansible-lint ${playbook} || echo 'Warning: ansible-lint reported issues.'"
    }
}
