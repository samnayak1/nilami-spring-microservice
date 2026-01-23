# Nilami

## Introduction

Nilami is a microservices-based auction application designed for bidding. The platform allows users to bid for items while providing administrators with the tools to list and manage sales.

The architecture is built using modern cloud-native technologies:

- **Backend:** Spring Boot, Spring Data JPA
- **Database Migration:** Flyway
- **Containerization:** Docker
- **Orchestration:** Kubernetes 
- **Security & Identity:** Keycloak
- **Secret Management:** Hashicorp Vault

---

## Prerequisites

The following tools must be installed to run the project:

1. Docker
2. Minikube/Kind/k3s
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
With K3s
```
curl -sfL https://get.k3s.io | sh -s - \
  --write-kubeconfig-mode=644

sudo kubectl get nodes
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
export KUBECONFIG=~/.kube/config

sudo systemctl stop k3s

sudo systemctl status k3s

sudo systemctl start k3s

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

helm list -n monitoring
# Vault Initialization & Unsealing
kubectl exec -n vault -it vault-0 -- sh
vault operator init
kubectl exec -n vault vault-0 -- vault status
kubectl exec -n vault vault-0 -- vault operator unseal <key1>
... until key n
kubectl exec -n vault vault-0 -- vault operator unseal <key2>

# Configure Policy and Tokens
kubectl exec -n vault -it vault-0 -- sh
vault login <ROOT_TOKEN>
kubectl cp vault-read.hcl vault/vault-0:/tmp/vault-read.hcl
kubectl exec -it -n vault vault-0 -- vault policy write vault-read /tmp/vault-read.hcl
kubectl create secret generic vault-token -n external-secrets --from-literal=token=<root token>


#to verify

 kubectl describe externalsecret <secret-name> -n <namspace>
 kubectl describe ClusterSecretStore vault-backend

#To view all secrets with their 
 kubectl get externalsecrets -A

#To restart the external secret
 kubectl rollout restart deployment external-secrets -n external-secrets

 
```



---




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



curl -v "http://app.local/ws/socket.io/?EIO=4&transport=polling"

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




## Monitoring

Prometheus - Collects and stores metrics (CPU usage, request rates, error counts, etc.)
Loki - Collects and stores logs (application logs, error messages, debug info)
Tempo - Collects and stores traces (tracking requests as they flow through your services)
OpenTelemetry - Instruments your applications to send metrics, logs, and traces to the above tools
Grafana - Visualizes everything in dashboards and handles alerting

app -> opentelemettry -> tempo

app -> Prometheus -> Grafana

app -> Loki -> Grafana

Receivers collect telemetry from one or more sources. They can be pull or push based, and may support one or more data sources.

Exporters send data to one or more backends or destinations. Exporters can be pull or push based, and may support one or more data sources. We use Loki and Prometheus

p50 (Median) - 50% of requests are faster than this. A “typical” user experience.
p95 - 95% of requests are faster. The other 5%? Could be slow, problematic, or timing out.
p99 - 99% of requests are faster. That remaining 1%? Usually what people complain about.

helm install monitoring grafana/loki-stack \
  --set prometheus.enabled=true \
  --set loki.enabled=true \
  --set grafana.enabled=true

  helm upgrade monitoring grafana/loki-stack \
  --set loki.image.tag=2.9.4 \
  --reuse-values

  kubectl rollout restart deployment -n monitoring

kubectl port-forward svc/monitoring-grafana 3000:80

kubectl get secret monitoring-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo

Go to explore

Prometheus

# Cluster CPU usage
sum(rate(container_cpu_usage_seconds_total[5m])) * 100

# Memory usage
sum(container_memory_working_set_bytes) / 1024 / 1024 / 1024

# Pod count
count(kube_pod_info)

# Node status
kube_node_status_condition{condition="Ready", status="true"}

Loki

# All logs
{job="varlogs"}

# Logs from specific pod (find pod names first)
kubectl get pods
# Then in Grafana:
{pod="your-pod-name-here"}

# Logs with errors
{job="varlogs"} |= "error"

# Logs from specific namespace
{namespace="default"}


Click "+" → "Import" → Enter these IDs:

Essential Dashboards:

3119 - Kubernetes cluster monitoring

6417 - Pod monitoring

315 - Node monitoring

12006 - Loki logs dashboard

10856 - Prometheus stats

13186 - Loki dashboard

ID 15141: Kubernetes Logs (Loki) – The gold standard for viewing logs by namespace, pod, and container.

ID 13186: Loki Dashboard – Includes a quick search bar and a log timeline.

ID 14055: Loki Stack Monitoring – Specifically designed for the loki-stack chart to monitor Loki's health itself.



 kubectl get pods -l app=loki

 kubectl get pods -l app.kubernetes.io/name=grafana






