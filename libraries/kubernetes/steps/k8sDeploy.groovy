// steps/deploy.groovy — Deploys Kubernetes manifests locally via SSH Tunnel with auto-fetched kubeconfig

void call(Map args = [:]) {
    String sshCreds     = args.ssh_creds     ?: config.ssh_creds     ?: 'ansible_ssh_key'
    String bastionUser  = args.bastion_user  ?: config.bastion_user  ?: 'ec2-user'
    String namespace    = args.namespace     ?: config.namespace    ?: 'default'
    String manifestDir  = args.manifests_dir ?: config.manifests_dir ?: '.'
    String targetFolder = args.target_folder ?: config.target_folder ?: '.'
    String deployName   = args.deployment    ?: config.deployment
    boolean waitForRollout = config.wait_for_rollout != null ? config.wait_for_rollout : true

    String inventoryFile = args.inventory_file ?: config.inventory_file ?: 'inventory.ini'

    if (!args.bastion_ip && !config.bastion_ip && !fileExists(inventoryFile)) {
        error "kubernetes.deploy requires '${inventoryFile}' in the workspace. Run fetchInventory() before deploy."
    }

    String bastionIp = args.bastion_ip ?: config.bastion_ip ?: sh(
        script: "grep -A1 '\\[bastion\\]' ${inventoryFile} | grep -v '\\[bastion\\]' | awk '{print \$2}' | cut -d'=' -f2 | tr -d '\\r\\n'",
        returnStdout: true
    ).trim()

    String masterIp = args.master_ip ?: config.master_ip ?: sh(
        script: "grep -A1 '\\[master\\]' ${inventoryFile} | grep -v '\\[master\\]' | awk '{print \$2}' | cut -d'=' -f2 | tr -d '\\r\\n'",
        returnStdout: true
    ).trim()

    if (!bastionIp || !masterIp) {
        error "kubernetes.deploy failed to extract 'bastion_ip' or 'master_ip' from ${inventoryFile}."
    }

    echo "Using Bastion IP '${bastionIp}' and Master IP '${masterIp}' from ${inventoryFile}."

    String deployPath = (targetFolder != '.' && manifestDir != '.') ? "${manifestDir}/${targetFolder}" : ((targetFolder != '.') ? targetFolder : manifestDir)

    withCredentials([sshUserPrivateKey(credentialsId: sshCreds, keyFileVariable: 'SSH_KEY')]) {
        sh """
            chmod 400 "\$SSH_KEY"
            
            # Start SSH tunnel in background
            ssh -f -N -L 6443:${masterIp}:6443 -o StrictHostKeyChecking=no -i "\$SSH_KEY" ${bastionUser}@${bastionIp}
            
            # Fetch kubeconfig from Master via ProxyCommand
            ssh -o StrictHostKeyChecking=no -i "\$SSH_KEY" \
                -o ProxyCommand="ssh -o StrictHostKeyChecking=no -i \"\$SSH_KEY\" -W %h:%p ${bastionUser}@${bastionIp}" \
                ${bastionUser}@${masterIp} \
                "cat ~/.kube/config 2>/dev/null || sudo cat /etc/kubernetes/admin.conf" > kubeconfig.tmp
            chmod 600 kubeconfig.tmp

            # Apply manifests
            KUBECONFIG=kubeconfig.tmp kubectl apply -f ${deployPath}/ -n ${namespace} --server=https://127.0.0.1:6443 --insecure-skip-tls-verify
        """

        if (waitForRollout && deployName) {
            sh "KUBECONFIG=kubeconfig.tmp kubectl rollout status deployment/${deployName} -n ${namespace} --server=https://127.0.0.1:6443 --insecure-skip-tls-verify --timeout=300s"
        }

        sh """
            rm -f kubeconfig.tmp
            pkill -f 'ssh -f -N -L 6443:${masterIp}' || true
        """
    }
}
