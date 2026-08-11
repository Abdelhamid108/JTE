// steps/archiveInventory.groovy
//
// Archives the Terraform-generated Ansible inventory (e.g. produced by a local_file /
// template_file resource in the .tf configuration) as a Jenkins build artifact, so a
// downstream ansible pipeline can retrieve it with the Copy Artifact plugin
// (see ansible/steps/fetchInventory.groovy).

void call() {
    String targetDir = config.ansible_dir ?: '.'
    String inventoryFile = config.inventory_file ?: 'inventory.ini'

    dir(targetDir) {


        echo "Archiving Ansible inventory '${inventoryFile}' for downstream consumption..."
        archiveArtifacts artifacts: "${inventoryFile}", fingerprint: true, allowEmptyArchive: false
    }
}
