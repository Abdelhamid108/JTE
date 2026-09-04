// steps/installTools.groovy — Verify / install tools required by the Terraform pipeline:
// pip3, Terraform, Checkov, AWS CLI v2.
// Each block is a no-op when the tool is already present on the agent image.

void call() {
    String tfVersion = config.terraform_version ?: '1.10.5'

    sh """#!/bin/bash
        set -e
        export PATH="\${HOME}/.local/bin:/usr/local/bin:\$PATH"
        echo "================================================================"
        echo "  installTools: verifying CI toolchain"
        echo "================================================================"

        # ── 0. pip3 ────────────────────────────────────────────────────────
        if ! command -v pip3 >/dev/null 2>&1; then
            echo "installTools: pip3 not found — installing via ensurepip/get-pip..."
            python3 -m ensurepip --upgrade 2>/dev/null || \
                curl -fsSL https://bootstrap.pypa.io/get-pip.py | python3 -
        fi
        # Upgrade pip — use --break-system-packages for Debian bookworm (PEP 668)
        python3 -m pip install --quiet --upgrade pip --break-system-packages 2>/dev/null || \
            python3 -m pip install --user --quiet --upgrade pip
        export PATH="\${HOME}/.local/bin:\$PATH"
        echo "[OK] pip \$(pip3 --version)"

        # ── 1. Terraform ───────────────────────────────────────────────────
        if ! command -v terraform >/dev/null 2>&1; then
            echo "installTools: Terraform not found — installing v${tfVersion}..."
            ARCH=\$(uname -m | sed 's/x86_64/amd64/;s/aarch64/arm64/')
            curl -fsSL "https://releases.hashicorp.com/terraform/${tfVersion}/terraform_${tfVersion}_linux_\${ARCH}.zip" -o /tmp/tf.zip
            unzip -q -o /tmp/tf.zip -d /usr/local/bin
            rm -f /tmp/tf.zip
            chmod +x /usr/local/bin/terraform
        fi
        echo "[OK] \$(terraform -version | head -1)"

        # ── 2. Checkov ─────────────────────────────────────────────────────
        if ! command -v checkov >/dev/null 2>&1; then
            echo "installTools: Checkov not found — installing via pip3..."
            pip3 install --quiet checkov --break-system-packages 2>/dev/null || \
                pip3 install --user --quiet checkov
        fi
        export PATH="\${HOME}/.local/bin:\$PATH"
        echo "[OK] checkov \$(checkov --version 2>&1 | head -1)"

        # ── 3. AWS CLI ─────────────────────────────────────────────────────
        if ! command -v aws >/dev/null 2>&1; then
            echo "installTools: AWS CLI not found — installing v2..."
            ARCH=\$(uname -m)
            if [ "\$ARCH" = "x86_64" ]; then
                curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
            else
                curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o /tmp/awscliv2.zip
            fi
            unzip -q /tmp/awscliv2.zip -d /tmp/awscli
            /tmp/awscli/aws/install --update
            rm -rf /tmp/awscliv2.zip /tmp/awscli
        fi
        echo "[OK] \$(aws --version)"

        echo "================================================================"
        echo "  installTools: all tools verified OK"
        echo "  pip3 | terraform | checkov | aws-cli"
        echo "================================================================"

    """
}
