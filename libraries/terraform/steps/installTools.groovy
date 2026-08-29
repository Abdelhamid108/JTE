// steps/installTools.groovy — Install Terraform and Checkov on the agent if not present

void call() {
    String tfVersion = config.terraform_version ?: '1.10.5'

    sh """
        export PATH="\${HOME}/.local/bin:/usr/local/bin:\$PATH"

        if ! command -v terraform >/dev/null 2>&1; then
            echo "installTools: Installing Terraform v${tfVersion}..."
            curl -fsSL "https://releases.hashicorp.com/terraform/${tfVersion}/terraform_${tfVersion}_linux_amd64.zip" -o /tmp/tf.zip
            unzip -q -o /tmp/tf.zip -d /usr/local/bin
            rm -f /tmp/tf.zip
            chmod +x /usr/local/bin/terraform
        fi

        if ! command -v checkov >/dev/null 2>&1; then
            echo "installTools: Installing Checkov..."
            pip3 install --user --quiet checkov
        fi

        terraform -version
        checkov --version
    """
}
