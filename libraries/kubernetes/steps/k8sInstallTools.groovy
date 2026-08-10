// steps/installTools.groovy — Install Kubernetes pipeline agent dependencies (kubectl, openssh-client)

void call() {
    echo "Installing kubectl, openssh-client, and git inside container..."
    sh '''
        if command -v apk >/dev/null 2>&1; then
            apk update && apk add --no-cache openssh-client git curl && \
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
            chmod +x kubectl && mv kubectl /usr/local/bin/
        else
            apt-get update && apt-get install -y openssh-client git curl && \
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
            chmod +x kubectl && mv kubectl /usr/local/bin/
        fi
    '''
}
