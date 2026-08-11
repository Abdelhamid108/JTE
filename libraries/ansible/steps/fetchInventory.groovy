// steps/fetchInventory.groovy
//
// Pulls the Terraform-generated Ansible inventory (hosts.ini) into this pipeline's
// workspace using the Copy Artifact plugin. The counterpart to this step is
// terraform's archiveInventory() step, which archives the same file as a build
// artifact on the terraform_infra job.

void call() {
    String terraformJob  = config.terraform_job_name
    String inventoryFile = config.inventory_file 
    String selector      = config.terraform_build_selector ?: 'lastSuccessful'
    String targetDir     = config.playbook_dir ?: '.'


    echo "Fetching Ansible inventory '${inventoryFile}' into '${targetDir}' from upstream job '${terraformJob}' (selector: ${selector})..."

    dir(targetDir) {
        copyArtifacts(
            projectName: terraformJob,
            selector: (selector == 'lastSuccessful') ? lastSuccessful() : specific(selector),
            filter: inventoryFile,
            fingerprintArtifacts: true,
            flatten: true
        )
    }

    echo "Inventory file '${inventoryFile}' retrieved successfully in '${targetDir}'"
}
