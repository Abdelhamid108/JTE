// steps/archiveInventory.groovy
//
// Archives the Terraform-generated Ansible inventory (e.g. produced by a local_file /
// template_file resource in the .tf configuration) as a Jenkins build artifact, so a
// downstream ansible pipeline can retrieve it with the Copy Artifact plugin
// (see ansible/steps/fetchInventory.groovy).

void call() {
    String targetDir = config.ansible_dir ?: '.'
    String inventoryFile = config.inventory_file ?: 'hosts.ini'

    dir(targetDir) {
        if (!fileExists(inventoryFile)) {
            error "PIPELINE STOPPED: Expected inventory file '${inventoryFile}' was not found in ${targetDir}. Check that your Terraform configuration writes it out (e.g. via a local_file resource) before this step runs."
        }

        echo "Archiving Ansible inventory '${inventoryFile}' for downstream consumption..."
        archiveArtifacts artifacts: "${inventoryFile}", fingerprint: true, allowEmptyArchive: false
    }
}
