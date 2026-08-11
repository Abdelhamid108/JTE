// steps/npmInstallTools.groovy — Install application pipeline agent dependencies

void call() {
    echo "Installing agent dependencies (AWS CLI, kubectl CLI, Docker CLI, Git)..."
    sh '''
        if command -v apk >/dev/null 2>&1; then
            apk update && apk add --no-cache curl git bash docker-cli aws-cli github-cli python3 py3-pip || pip3 install awscli --break-system-packages
        else
            apt-get update && apt-get install -y curl git docker.io awscli gh || true
        fi

        if ! command -v gh >/dev/null 2>&1; then
            echo "Installing GitHub CLI..."
            curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg 2>/dev/null || true
            chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg 2>/dev/null || true
            echo "deb [arch=$(dpkg --print-architecture 2>/dev/null || echo amd64) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null 2>/dev/null || true
            apt-get update 2>/dev/null && apt-get install -y gh 2>/dev/null || true
        fi

        if ! command -v kubectl >/dev/null 2>&1; then
            echo "Installing kubectl CLI..."
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
            chmod +x kubectl
            mv kubectl /usr/local/bin/ || mv kubectl /usr/bin/
        fi
    '''
}
