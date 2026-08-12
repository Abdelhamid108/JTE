// steps/installTools.groovy — Install Terraform pipeline agent dependencies

void call(Map args = [:]) {
    echo "Installing Checkov and agent dependencies inside container..."
    sh '''
        if command -v apk >/dev/null 2>&1; then
            apk update && apk add --no-cache python3 py3-pip git && pip3 install checkov --break-system-packages
        else
            apt-get update && apt-get install -y python3 python3-pip git && pip3 install checkov --break-system-packages || true
        fi
    '''
}
