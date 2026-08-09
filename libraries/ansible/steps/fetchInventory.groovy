// steps/fetchInventory.groovy
//
// Pulls the Terraform-generated Ansible inventory (hosts.ini) into this pipeline's
// workspace using the Copy Artifact plugin. The counterpart to this step is
// terraform's archiveInventory() step, which archives the same file as a build
// artifact on the terraform_infra job.

void call(Map args = [:]) {
    String terraformJob  = args.terraform_job_name ?: config.terraform_job_name
    String inventoryFile = args.inventory_file ?: config.inventory_file ?: 'hosts.ini'
    String selector      = args.terraform_build_selector ?: config.terraform_build_selector ?: 'lastSuccessful'
    String targetDir     = args.playbook_dir ?: config.playbook_dir ?: '.'

    if (!terraformJob) {
        error "PIPELINE STOPPED: 'terraform_job_name' must be set in the ansible library config (pipeline_config.groovy) so fetchInventory() knows which upstream job archived the inventory."
    }

    echo "Fetching Ansible inventory '${inventoryFile}' into '${targetDir}' from upstream job '${terraformJob}' (selector: ${selector})..."

    dir(targetDir) {
        copyArtifacts(
            projectName: terraformJob,
            selector: (selector == 'lastSuccessful') ? lastSuccessful() : specific(selector),
            filter: inventoryFile,
            fingerprintArtifacts: true,
            flatten: true
        )

        if (!fileExists(inventoryFile)) {
            error "PIPELINE STOPPED: Inventory file '${inventoryFile}' was not found in '${targetDir}' after copyArtifacts() from '${terraformJob}'. Confirm the terraform_infra job has a successful build that ran archiveInventory()."
        }
    }

    echo "Inventory file '${inventoryFile}' retrieved successfully in '${targetDir}'."
}
