# Nilami

## Introduction

Nilami is a microservices-based auction application designed for bidding. The platform allows users to bid for items while providing administrators with the tools to list and manage sales.



The architecture is built using:

- **Backend:** Spring Boot, Spring Data JPA
- **Database Migration:** Flyway
- **Containerization:** Docker
- **Orchestration:** Kubernetes 
- **Security & Identity:** AWS Cognito
- **File Storage:** AWS s3
- **Secret Management:** Hashicorp Vault

![Nilami Architechture](nilami.svg)
---

## Prerequisites

The following tools must be installed to run the project:

1. Docker
2. k3s
3. Helm

---

## Local Development Setup

### Create a New Service

To bootstrap a new microservice using the Spring Initializr API:
```bash
curl https://start.spring.io/starter.zip \
  -d language=java \
  -d type=maven-project \
  -d groupId=com.nilami \
  -d artifactId=<service-id> \
  -d name=<name-of-service> \
  -d packageName=com.nilami.<name-of-service> \
  -d javaVersion=21 \
  -o <filename>.zip
```

### Start Infrastructure

Start the local Kubernetes cluster with the required resources using K3s:
```bash
# Install K3s
curl -sfL https://get.k3s.io | sh -s - --write-kubeconfig-mode=644

# Verify installation
sudo kubectl get nodes

# Configure kubectl
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
export KUBECONFIG=~/.kube/config

# Manage K3s service
sudo systemctl stop k3s
sudo systemctl status k3s
sudo systemctl start k3s
```

---
### Documentation

## Swagger
Forward the port 8084 of the service api-gateway

```bash
kubectl port-forward svc/api-gateway 8084:8084
```
Then visit

```bash
 http://localhost:8080/swagger-ui.html
 ```



---
## Image Management

### Docker Hub
```bash
# Build image
docker build -t <imagename>:latest .

# Tag image
docker tag <imagename>:latest <dockerhub-username>/<imagename>:<version>

# Push to Docker Hub
docker push <dockerhub-username>/<image-name>:<version>
```



---

## Secret Management

### Hashicorp Vault (Current Standard)

The project uses Hashicorp Vault with the External Secrets Operator.

#### Install Vault
```bash
# Add Hashicorp Helm repository
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

# Install Vault
helm install vault hashicorp/vault -n vault --create-namespace -f vault-values.yaml

# Port forward to access Vault
kubectl port-forward -n vault svc/vault 8200:8200
```

#### Install External Secrets Operator
```bash
# Add External Secrets Helm repository
helm repo add external-secrets https://charts.external-secrets.io

# Install External Secrets
helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets --create-namespace

# List Helm releases in monitoring namespace
helm list -n monitoring
```

#### Initialize and Unseal Vault
```bash
# Access Vault pod
kubectl exec -n vault -it vault-0 -- sh

# Initialize Vault
vault operator init

# Check Vault status
kubectl exec -n vault vault-0 -- vault status

# Unseal Vault (repeat with different keys until unsealed)
kubectl exec -n vault vault-0 -- vault operator unseal <key1>
kubectl exec -n vault vault-0 -- vault operator unseal <key2>
# ... continue until unsealed
```

#### Configure Vault Policy and Tokens
```bash
# Access Vault pod
kubectl exec -n vault -it vault-0 -- sh

# Login with root token
vault login <ROOT_TOKEN>

# Copy policy file to Vault pod
kubectl cp vault-read.hcl vault/vault-0:/tmp/vault-read.hcl

# Create policy
kubectl exec -it -n vault vault-0 -- vault policy write vault-read /tmp/vault-read.hcl

# Create Kubernetes secret with Vault token
kubectl create secret generic vault-token -n external-secrets --from-literal=token=<root-token>
```

#### Verify Vault Configuration
```bash
# Describe external secret
kubectl describe externalsecret <secret-name> -n <namespace>

# Describe cluster secret store
kubectl describe ClusterSecretStore vault-backend

# View all external secrets
kubectl get externalsecrets -A

# Restart External Secrets deployment
kubectl rollout restart deployment external-secrets -n external-secrets
```

---

## Database (CloudNativePG)
```bash
# Install CloudNativePG Operator
kubectl apply --server-side -f \
  https://raw.githubusercontent.com/cloudnative-pg/cloudnative-pg/release-1.28/releases/cnpg-1.28.0.yaml

# Port forward to database
kubectl port-forward svc/catalog-db-rw 5432:5432 &

# Access database
kubectl exec -it catalog-db-1 -- psql -U postgres -c "\l"

# Connect to database directly
psql -h localhost -p 5432 -U <user> -d <database>
```

---

## Ingress

### Install Ingress Controller
```bash
# Install NGINX Ingress Controller for Kind
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

### Test Ingress
```bash
# Test WebSocket connection
curl -v "http://app.local/ws/socket.io/?EIO=4&transport=polling"
```

### Install cert-manager
```bash
# Add Jetstack Helm repository
helm repo add jetstack https://charts.jetstack.io
helm repo update

# Install cert-manager CRDs
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.3/cert-manager.crds.yaml

# Install cert-manager
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.13.3
```

### Verify cert-manager
```bash
# Check cluster issuers
kubectl get clusterissuer

# Check certificates
kubectl get certificate -n default

# Describe specific certificate
kubectl describe certificate app-local-tls -n default

# Get domain information
kubectl get svc -n kube-system | grep traefik
```

---

## Troubleshooting & Operations

### Common Kubernetes Commands

#### Restart Deployment
```bash
# Restart specific deployment
kubectl rollout restart deployment/<deployment-name>

# Restart External Secrets deployment
kubectl rollout restart deployment external-secrets -n external-secrets
```

#### Monitor Pods
```bash
# Watch pods in real-time
kubectl get pods -w
```

#### Access Pod Shell
```bash
# Bash into a pod
kubectl exec -it -n <namespace> <pod> -- bash
```


#### Port Forwarding
```bash
# Forward API Gateway port
kubectl port-forward svc/api-gateway 8084:8084
```

#### Resource Usage
```bash
# Enable metrics server (Minikube)
minikube addons enable metrics-server

# View pod resource usage
kubectl top pods --all-namespaces --sort-by=memory
```

### Cleanup
```bash
# Scale down deployment to zero replicas
kubectl scale deployment <deployment-name> --replicas=0

# Clear kubectl cache
rm -rf ~/.kube/cache ~/.kube/http-cache
```

---

## Monitoring (not used right now)

The monitoring stack includes:

- **Prometheus:** Collects and stores metrics (CPU usage, request rates, error counts, etc.)
- **Loki:** Collects and stores logs (application logs, error messages, debug info)
- **Grafana:** Visualizes everything in dashboards and handles alerting

### Install Monitoring Stack
```bash
# Install Loki stack with Prometheus and Grafana
helm install monitoring grafana/loki-stack \
  --set prometheus.enabled=true \
  --set loki.enabled=true \
  --set grafana.enabled=true

# Upgrade Loki version
helm upgrade monitoring grafana/loki-stack \
  --set loki.image.tag=2.9.4 \
  --reuse-values

# Upgrade with custom values file
helm upgrade monitoring grafana/loki-stack -f monitoring-values.yaml

# Delete Prometheus node exporter daemonset (if needed)
kubectl delete daemonset monitoring-prometheus-node-exporter

# Restart monitoring deployments
kubectl rollout restart deployment -n monitoring
```

### Access Grafana
```bash
# Port forward Grafana service
kubectl port-forward svc/monitoring-grafana 3000:80

# Get Grafana admin password
kubectl get secret monitoring-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
```

---

## Quick Reference

### Common Operations

| Task | Command |
|------|---------|
| Access pod shell | `kubectl exec -it -n <namespace> <pod> -- bash` |
| Connect to database | `psql -h localhost -p 5432 -U <user> -d <database>` |
| Port forward service | `kubectl port-forward svc/<service-name> <local-port>:<service-port>` |
| View logs | `kubectl logs -f <pod-name> -n <namespace>` |
| Get pod status | `kubectl get pods -n <namespace>` |
| Describe resource | `kubectl describe <resource-type> <resource-name> -n <namespace>` |

---