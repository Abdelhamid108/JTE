// steps/installTools.groovy — Automatically installs Terraform and Checkov on the agent.

void call(Map args = [:]) {
    String tfVersion = args.terraform_version ?: config.terraform_version ?: '1.10.5'

    echo "Checking agent dependencies (Terraform & Checkov)..."

    sh """
        export PATH="\${HOME}/.local/bin:/usr/local/bin:\$PATH"

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
            elif command -v python3 >/dev/null 2>&1; then
                python3 -c "import zipfile; zipfile.ZipFile('/tmp/terraform.zip').extractall('\$TARGET_DIR')"
            fi
            
            chmod +x "\$TARGET_DIR/terraform"
            rm -f /tmp/terraform.zip
            echo "installTools: Terraform installed to \$TARGET_DIR/terraform"
        fi

        # 2. Install Checkov if not present
        if ! command -v checkov >/dev/null 2>&1; then
            echo "installTools: Checkov not found in PATH. Installing Checkov..."
            if ! command -v pip3 >/dev/null 2>&1 && ! command -v pip >/dev/null 2>&1; then
                if command -v apt-get >/dev/null 2>&1 && [ "\$(id -u)" -eq 0 ]; then
                    apt-get update -qq && apt-get install -y -qq python3-pip
                elif command -v apk >/dev/null 2>&1 && [ "\$(id -u)" -eq 0 ]; then
                    apk add --no-cache py3-pip
                fi
            fi

            pip3 install --break-system-packages --ignore-installed checkov 2>/dev/null \
                || pip3 install --user --ignore-installed checkov 2>/dev/null \
                || pip install --user --ignore-installed checkov 2>/dev/null \
                || true

            # If installed to ~/.local/bin and /usr/local/bin is writable, create a symlink
            if [ -f "\${HOME}/.local/bin/checkov" ] && [ -w "/usr/local/bin" ] && [ ! -f "/usr/local/bin/checkov" ]; then
                ln -sf "\${HOME}/.local/bin/checkov" /usr/local/bin/checkov
            fi
        fi

        # 3. Verify tools
        export PATH="\${HOME}/.local/bin:/usr/local/bin:\$PATH"
        if command -v terraform >/dev/null 2>&1; then
            terraform -version
        else
            echo "Warning: Terraform not found in PATH"
        fi

        if command -v checkov >/dev/null 2>&1; then
            checkov --version
        else
            echo "Warning: Checkov could not be verified in PATH. Ensure ~/.local/bin or Python bin is in PATH."
        fi
    """
}
