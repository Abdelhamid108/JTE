// steps/installTools.groovy — Install application pipeline agent dependencies

void call(Map args = [:]) {
    echo "Installing agent dependencies (AWS CLI, kubectl CLI, Docker CLI, Git)..."
    sh '''
        if command -v apk >/dev/null 2>&1; then
            apk update && apk add --no-cache curl git bash docker-cli aws-cli python3 py3-pip || pip3 install awscli --break-system-packages
        else
            apt-get update && apt-get install -y curl git docker.io awscli || true
        fi

        if ! command -v kubectl >/dev/null 2>&1; then
            echo "Installing kubectl CLI..."
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
            chmod +x kubectl
            mv kubectl /usr/local/bin/ || mv kubectl /usr/bin/
        fi
    '''
}
