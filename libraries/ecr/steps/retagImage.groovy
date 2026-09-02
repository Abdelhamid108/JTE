void call(Map args = [:]) {
    String repository = args.repository ?: pipelineConfig.libraries.docker.image_name
    String sourceTag  = args.source_tag
    String targetTag  = args.target_tag
    String region     = args.region ?: config.aws_region 

    echo "Retagging in ECR: ${repository}:${sourceTag} -> ${targetTag}"

    sh """
        MANIFEST=\$(aws ecr batch-get-image \
            --repository-name "${repository}" \
            --image-ids imageTag="${sourceTag}" \
            --region "${region}" \
            --query 'images[].imageManifest' \
            --output text)

        if [ -z "\$MANIFEST" ] || [ "\$MANIFEST" = "None" ]; then
            echo "ERROR: Source image tag ${sourceTag} not found in ECR!"
            exit 1
        fi

        aws ecr put-image \
            --repository-name "${repository}" \
            --image-tag "${targetTag}" \
            --image-manifest "\$MANIFEST" \
            --region "${region}"
    """
}
