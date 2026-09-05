# Ansible Library (`ansible`)

The `ansible` library provides configuration management by executing Ansible playbooks against hosts provisioned by Terraform.

---

## 1. Configuration Schema (`library_config.groovy`)

```groovy
fields {
    optional {
        playbook_dir             = String   // Playbook directory (default: "ansible")
        playbook_file            = String   // Playbook entrypoint (default: "site.yml")
        inventory_file           = String   // Inventory file (default: "inventory.ini")
        ssh_creds                = String   // Jenkins SSH private key credential ID
        terraform_job_name       = String   // Upstream Terraform job name
        terraform_build_selector = String   // Build selector: "lastSuccessful"
        install_tools            = Boolean  // Install Ansible on agent
        become                   = Boolean  // Run with --become (default: true)
    }
}
```

---

## 2. Steps Reference

### `ansibleCheckoutCode`
Checks out the repository containing Ansible playbooks.
- **Signature**: `void call()`

---

### `ansibleInstallTools`
Installs Ansible, `ansible-lint`, and SSH tooling on the agent.
- **Signature**: `void call()`

---

### `fetchInventory`
Copies the `inventory.ini` artifact from the upstream `terraform` job using the Jenkins Copy Artifact plugin.
- **Signature**: `void call()`

---

### `ansibleLint`
Executes syntax checking and `ansible-lint` against playbooks.
- **Signature**: `void call()`

---

### `ansibleDeploy`
Executes `ansible-playbook` with SSH key binding via `sshagent`.
- **Signature**: `void call()`

---

## 3. Pipeline Configuration Example

```groovy
libraries {
    ansible {
        playbook_dir       = "ansible"
        playbook_file      = "site.yml"
        inventory_file     = "inventory.ini"
        ssh_creds          = "ansible-ssh-key"
        terraform_job_name = "Atos CI-CD Project/test-infra/main"
        install_tools      = true
        become             = true
    }
}
```\n