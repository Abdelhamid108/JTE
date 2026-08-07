pipeline {
    agent {
        docker {
            image "hashicorp/terraform:light"
            args '-u root:root --entrypoint='
        }
    }
    
    environment {
        TARGET_ENV = env.BRANCH_NAME == 'main' ? 'prod' : 'dev'
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                checkout()
            }
        }
        
        stage('Install Dependencies') {
            when { 
                expression { return pipelineConfig.terraform?.install_tools == true }
            }
            steps {
                echo "Installing Checkov inside the container on the fly..."
                sh 'apk update && apk add --no-cache python3 py3-pip git && pip3 install checkov'
            }
        }
        
        stage('Initialize & Validate') {
            steps {
                init()
                validate()
            }
        }
        
        stage('Security Scan') {
            steps {
                checkov()
            }
        }
        
        stage('Plan Infrastructure') {
            steps { 
                plan()       
            }
        }

        stage('Approval Guardrail') {
            when {
                branch 'dev'
                branch 'main'
            }
            steps { 
                approval()   
            }
        }
        
        stage('Execute Deploy / Destroy') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'main'
                }
            }
            steps {
                script {
                    boolean isDestroy = pipelineConfig.terraform?.is_destroy ?: false
                    if (isDestroy) {
                        destroy() 
                    } else {
                        deploy()  
                    }
                }
            }
        }
    } 
    
    post {
        always {
            cleanWs()
        }
    }
}