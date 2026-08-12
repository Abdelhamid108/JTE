// steps/ansibleLint.groovy — Syntax-checks and lints the playbook before it is run against real hosts

void call() {
    String targetDir = config.playbook_dir ?: '.'
    String playbook  = config.playbook_file
    String inventory = config.inventory_file

    dir(targetDir) {

        echo "Running ansible-playbook syntax check..."
        sh "ansible-playbook -i ${inventory} ${playbook} --syntax-check"

        echo "Running ansible-lint..."
        sh "ansible-lint ${playbook} || echo 'Warning: ansible-lint reported issues.'"
    }
}
