void call ( List imagesToClean = []){
    echo "Cleaning up local Docker images to save disk space..."
    
    if (imagesToClean){
        imagesToClean.each { ing ->
            sh "docker rmi ${img} || true "
        }
    } 
    }