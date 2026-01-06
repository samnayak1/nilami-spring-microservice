# Nilami

## Introduction

Nilami is a microservices-based auction application designed for bidding. The platform allows users to bid for items while providing administrators with the tools to list and manage sales.

The architecture is built using modern cloud-native technologies:

- **Backend:** Spring Boot, Spring Data JPA
- **Database Migration:** Flyway
- **Containerization:** Docker
- **Orchestration:** Kubernetes (Minikube)
- **Security & Identity:** Keycloak
- **Secret Management:** Hashicorp Vault

---

## Prerequisites

The following tools must be installed to run the project:

1. Docker
2. Minikube
3. Helm
4. Kubeseal

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

Start the local Kubernetes cluster with the required resources:

With minikube
```bash
minikube start \
  --driver=docker \
  --memory=4096 \
  --cpus=2 \
  --disk-size=25g
```
With Kind

```bash
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.23.0/kind-linux-amd64
chmod +x kind
sudo mv kind /usr/local/bin/


kind create cluster

kubectl config current-context
```

---

## Image Management

### Docker Hub

```bash
docker build -t <imagename>:latest .
docker tag <imagename>:latest <dockerhub-username>/<imagename>:<version>
docker push <dockerhub-username>/<image-name>:<version>
```

### Run Locally (Standalone)

```bash
docker run --name <name-of-image> \
  -p 8084:8084 \
  <name-of-image>:latest
```

### AWS ECR (Alternative)

```bash
# Setup AWS CLI
sudo snap install aws-cli --classic
aws configure

# Authentication
aws ecr get-login-password --region <region> | \
  docker login --username AWS --password-stdin \
  <account-number>.dkr.ecr.<region>.amazonaws.com

# Push to ECR
docker tag <name-of-app>:latest \
  <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/dev/samnayak1:v1
docker push <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/dev/samnayak1:v1
```

### Local Minikube Registry (Alternative)

To build directly into the Minikube node:

```bash
eval $(minikube docker-env)
docker build -t <name-of-image>:latest .
eval $(minikube docker-env -u)
```

---

## Secret Management

### Hashicorp Vault (Current Standard)

The project uses Hashicorp Vault with the External Secrets Operator.

```bash
# Install Vault via Helm
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update
helm install vault hashicorp/vault -n vault --create-namespace -f vault-values.yaml
kubectl port-forward -n vault svc/vault 8200:8200
# Install External Secrets
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets --create-namespace

# Vault Initialization & Unsealing
kubectl exec -n vault -it vault-0 -- sh
vault operator init
kubectl exec -n vault vault-0 -- vault status
kubectl exec -n vault vault-0 -- vault operator unseal <key>

# Configure Policy and Tokens
kubectl exec -n vault -it vault-0 -- sh
vault login <ROOT_TOKEN>
kubectl cp vault-read.hcl vault/vault-0:/tmp/vault-read.hcl
kubectl exec -it -n vault vault-0 -- vault policy write vault-read /tmp/vault-read.hcl
kubectl create secret generic vault-token -n external-secrets --from-literal=token=<token>


 kubectl get externalsecret 
 kubectl describe externalsecret <secret-name>
```

### Bitnami Sealed Secrets (Deprecated)

> **Note:** This method is deprecated in favor of Hashicorp Vault.

```bash
kubeseal --format=yaml < <unsealed-file>.yaml \
  --controller-namespace kube-system \
  --controller-name sealed-secrets > <sealed-file>.yaml
kubectl apply -f <sealed-file>.yaml
```

---

## Infrastructure Services

### Messaging (Kafka)

```bash
# Install
helm install kafka bitnami/kafka -f kafka-values.yaml \
  --namespace kafka --create-namespace

# Upgrade
helm upgrade kafka bitnami/kafka -f kafka-values.yaml --namespace kafka

# Check Environment
kubectl exec <pod-name> -- printenv | grep KAFKA_BROKER
```

### Database (CloudNativePG)

```bash
# Install Operator
kubectl apply --server-side -f \
  https://raw.githubusercontent.com/cloudnative-pg/cloudnative-pg/release-1.28/releases/cnpg-1.28.0.yaml

# Database Access
kubectl port-forward svc/catalog-db-rw 5432:5432 &
kubectl exec -it catalog-db-1 -- psql -U postgres -c "\l"
```
### Ingress
```bash
minikube addons enable ingress


kubectl get pods -n ingress-nginx

```




---

## Troubleshooting & Operations

### Common Kubernetes Commands

**Restart Deployment:**
```bash
kubectl rollout restart deployment/<deployment-name>

kubectl rollout restart deployment external-secrets -n external-secrets
```

**Monitor Pods:**
```bash
kubectl get pods -w
```

**Internal Routing Test:**
```bash
kubectl exec -it <pod-name> -- curl -v http://registry-service:8761/eureka
```

**Port Forwarding:**
```bash
kubectl port-forward svc/api-gateway 8084:8084
```

**Resource Usage:**
```bash
minikube addons enable metrics-server
kubectl top pods --all-namespaces --sort-by=memory
```

### Cleanup

```bash
# Delete Pods by scaling down
kubectl scale deployment <deployment-name> --replicas=0

# Clear Kubectl Cache
rm -rf ~/.kube/cache ~/.kube/http-cache
```

---

To bash into a pod

kubectl exec -it -n <namespace> <pod> -- bash

To enter the database 
psql -h localhost -p 5432 -U <user> -d <database>


To install the ingress
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

