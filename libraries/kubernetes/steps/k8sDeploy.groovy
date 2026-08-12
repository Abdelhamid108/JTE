// steps/k8sDeploy.groovy — Deploys Kubernetes manifests via SSH Tunnel with auto-fetched kubeconfig

void call(Map args = [:]) {
    String sshCreds      = config.ssh_creds
    String bastionUser   = config.bastion_user ?: 'ec2-user'
    String manifestDir   = config.manifests_dir ?: '.'
    String targetFolder  = args.target_folder
    String deployName    = config.deployment
    String namespace     = args.namespace ?: config.namespace ?: 'default'
    boolean waitForRollout = config.wait_for_rollout != null ? config.wait_for_rollout : true
    String inventoryFile = config.inventory_file 

    String bastionIp = config.bastion_ip ?: sh(
        script: "grep -A1 '\\[bastion\\]' ${inventoryFile} | grep -v '\\[bastion\\]' | awk '{print \$2}' | cut -d'=' -f2 | tr -d '\\r\\n'",
        returnStdout: true
    ).trim()

    String masterIp = config.master_ip ?: sh(
        script: "grep -A1 '\\[master\\]' ${inventoryFile} | grep -v '\\[master\\]' | awk '{print \$2}' | cut -d'=' -f2 | tr -d '\\r\\n'",
        returnStdout: true
    ).trim()

    withCredentials([sshUserPrivateKey(credentialsId: sshCreds, keyFileVariable: 'SSH_KEY')]) {
        sh """
            chmod 400 "\$SSH_KEY"
            
            ssh -f -N -L 6443:${masterIp}:6443 -o StrictHostKeyChecking=no -i "\$SSH_KEY" ${bastionUser}@${bastionIp}
            
            ssh -o StrictHostKeyChecking=no -i "\$SSH_KEY" \\
                -o ProxyCommand="ssh -o StrictHostKeyChecking=no -i \\"\$SSH_KEY\\" -W %h:%p ${bastionUser}@${bastionIp}" \\
                ${bastionUser}@${masterIp} \\
                "cat ~/.kube/config 2>/dev/null || sudo cat /etc/kubernetes/admin.conf" > kubeconfig.tmp
            chmod 600 kubeconfig.tmp

            KUBECONFIG=kubeconfig.tmp kubectl apply -f ${manifestDir}/${targetFolder}/ -n ${namespace} --server=https://127.0.0.1:6443 --insecure-skip-tls-verify
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
