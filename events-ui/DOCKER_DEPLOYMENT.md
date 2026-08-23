# Docker & Kubernetes Deployment Guide

## Building the Docker Image

```bash
# Build the image
docker build -t events-ui:latest .

# Build with a specific tag
docker build -t your-registry/events-ui:v1.0.0 .

# Test the image locally
docker run -p 8080:80 events-ui:latest
# Access at http://localhost:8080
```

## Pushing to a Container Registry

### Docker Hub
```bash
docker tag events-ui:latest your-username/events-ui:latest
docker push your-username/events-ui:latest
```

### Google Container Registry (GCR)
```bash
docker tag events-ui:latest gcr.io/your-project-id/events-ui:latest
docker push gcr.io/your-project-id/events-ui:latest
```

### AWS ECR
```bash
aws ecr get-login-password --region region | docker login --username AWS --password-stdin aws_account_id.dkr.ecr.region.amazonaws.com
docker tag events-ui:latest aws_account_id.dkr.ecr.region.amazonaws.com/events-ui:latest
docker push aws_account_id.dkr.ecr.region.amazonaws.com/events-ui:latest
```

### Azure Container Registry (ACR)
```bash
az acr login --name your-registry-name
docker tag events-ui:latest your-registry-name.azurecr.io/events-ui:latest
docker push your-registry-name.azurecr.io/events-ui:latest
```

## Deploying to Kubernetes

1. **Update the image in k8s-deployment.yaml:**
   ```yaml
   image: your-registry/events-ui:latest
   ```

2. **Update the ingress host:**
   ```yaml
   host: events-ui.example.com  # Replace with your actual domain
   ```

3. **Apply the Kubernetes configuration:**
   ```bash
   kubectl apply -f k8s-deployment.yaml
   ```

4. **Verify the deployment:**
   ```bash
   # Check deployment status
   kubectl get deployments
   
   # Check pods
   kubectl get pods
   
   # Check service
   kubectl get services
   
   # Check ingress
   kubectl get ingress
   
   # View logs
   kubectl logs -l app=events-ui
   ```

5. **Scale the deployment:**
   ```bash
   kubectl scale deployment events-ui --replicas=3
   ```

## Environment-Specific Configuration

If you need different configurations for different environments, you can use ConfigMaps:

```bash
# Create a ConfigMap from config.json
kubectl create configmap events-ui-config --from-file=src/assets/config.json

# Or create inline
kubectl create configmap events-ui-config \
  --from-literal=API_URL=https://api.production.com
```

Then mount it in your deployment:
```yaml
volumeMounts:
- name: config
  mountPath: /usr/share/nginx/html/assets/config.json
  subPath: config.json
volumes:
- name: config
  configMap:
    name: events-ui-config
```

## Monitoring and Debugging

```bash
# Access a pod shell
kubectl exec -it <pod-name> -- sh

# Port forward to access locally
kubectl port-forward service/events-ui-service 8080:80

# View events
kubectl get events --sort-by='.lastTimestamp'

# Describe pod for details
kubectl describe pod <pod-name>
```

## Health Checks

The application includes:
- **Health endpoint:** `/health` - Returns 200 OK when healthy
- **Liveness probe:** Checks if the container is running
- **Readiness probe:** Checks if the container is ready to serve traffic

## Image Optimization

The Dockerfile uses:
- **Multi-stage build** to reduce final image size
- **Alpine Linux** for minimal footprint
- **nginx:alpine** for efficient serving
- **.dockerignore** to exclude unnecessary files

## Security Best Practices

✅ Running as non-root user (nginx default)
✅ Security headers configured in nginx
✅ No sensitive data in the image
✅ Resource limits defined in k8s deployment
✅ Health checks configured

## Troubleshooting

**Build fails:**
- Ensure Node.js dependencies are compatible
- Check if `--legacy-peer-deps` is needed
- Verify the build output path in angular.json

**Container won't start:**
- Check logs: `docker logs <container-id>`
- Verify nginx config: `docker run -it events-ui:latest nginx -t`

**404 errors in Kubernetes:**
- Verify ingress configuration
- Check service endpoints: `kubectl get endpoints`
- Ensure ingress controller is installed
