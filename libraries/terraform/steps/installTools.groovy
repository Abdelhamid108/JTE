// steps/installTools.groovy — Automatically installs Terraform and Checkov on the agent.

void call(Map args = [:]) {
    String tfVersion = args.terraform_version ?: config.terraform_version ?: '1.10.5'

    echo "Checking agent dependencies (Terraform & Checkov)..."

    sh """
        # 1. Install Terraform if not present
        if ! command -v terraform >/dev/null 2>&1; then
            echo "installTools: Terraform not found in PATH. Installing Terraform v${tfVersion}..."
            
            TARGET_DIR="/usr/local/bin"
            if [ ! -w "\$TARGET_DIR" ]; then
                TARGET_DIR="\${HOME}/.local/bin"
                mkdir -p "\$TARGET_DIR"
            fi

            curl -fsSL "https://releases.hashicorp.com/terraform/${tfVersion}/terraform_${tfVersion}_linux_amd64.zip" -o /tmp/terraform.zip
            
            if command -v unzip >/dev/null 2>&1; then
                unzip -q -o /tmp/terraform.zip -d "\$TARGET_DIR"
            else
                python3 -c "import zipfile; zipfile.ZipFile('/tmp/terraform.zip').extractall('\$TARGET_DIR')"
            fi
            
            chmod +x "\$TARGET_DIR/terraform"
            rm -f /tmp/terraform.zip
            echo "installTools: Terraform installed to \$TARGET_DIR/terraform"
        fi

        # 2. Install Checkov if not present
        if ! command -v checkov >/dev/null 2>&1; then
            echo "installTools: Checkov not found in PATH. Installing Checkov..."
            pip3 install --break-system-packages checkov 2>/dev/null || pip3 install --user checkov 2>/dev/null || pip install --user checkov 2>/dev/null || true
        fi

        # 3. Verify tools
        if command -v terraform >/dev/null 2>&1; then
            terraform -version
        else
            export PATH="\${HOME}/.local/bin:\$PATH"
            terraform -version || echo "Warning: Please ensure ~/.local/bin is in PATH"
        fi
    """
}
