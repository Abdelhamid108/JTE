// steps/installTools.groovy — Install Ansible pipeline agent dependencies

void call() {
    echo "Installing Ansible and agent dependencies inside container..."
    sh '''
        if command -v apk >/dev/null 2>&1; then
            apk update && apk add --no-cache python3 py3-pip openssh-client git && pip3 install ansible ansible-lint --break-system-packages
        else
            apt-get update && apt-get install -y python3 python3-pip openssh-client git && pip3 install ansible ansible-lint --break-system-packages || true
        fi
    '''
}
