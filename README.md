# Nilami

Nilami is a microservices-based auction platform that allows users to bid on items while giving administrators the tools to list and manage sales.

**Tech Stack:**
- **Backend:** Spring Boot, Spring Data JPA
- **Database Migration:** Flyway
- **Containerization:** Docker
- **Orchestration:** Kubernetes
- **Security & Identity:** AWS Cognito
- **File Storage:** AWS S3
- **Secret Management:** HashiCorp Vault

**Client:** [nilami-dashboard](https://github.com/samnayak1/nilami-dashboard)

---

## Prerequisites

- Docker
- k3s
- Helm

---

## Local Development

### Create a New Service

Bootstrap a new microservice using the Spring Initializr API:

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

# Manage the K3s service
sudo systemctl start k3s
sudo systemctl stop k3s
sudo systemctl status k3s
```

---

## API Documentation (Swagger)

Port-forward the API gateway, then open the Swagger UI:

```bash
kubectl port-forward svc/api-gateway 8084:8084
```

Visit: `http://localhost:8080/swagger-ui.html`

---

## Image Management

```bash
# Build
docker build -t <imagename>:latest .

# Tag
docker tag <imagename>:latest <dockerhub-username>/<imagename>:<version>

# Push
docker push <dockerhub-username>/<imagename>:<version>
```

---

## Secret Management (HashiCorp Vault)

### Install Vault

```bash
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update
helm install vault hashicorp/vault -n vault --create-namespace -f vault-values.yaml

# Access the Vault UI
kubectl port-forward -n vault svc/vault 8200:8200
```

### Install External Secrets Operator

```bash
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets --create-namespace
```

### Initialize and Unseal Vault

```bash
# Shell into the Vault pod
kubectl exec -n vault -it vault-0 -- sh

# Initialize
vault operator init

# Check status
kubectl exec -n vault vault-0 -- vault status

# Unseal (run with different keys until unsealed)
kubectl exec -n vault vault-0 -- vault operator unseal <key1>
kubectl exec -n vault vault-0 -- vault operator unseal <key2>
```

### Configure Vault Policy and Token

```bash
# Shell into Vault and log in
kubectl exec -n vault -it vault-0 -- sh
vault login <ROOT_TOKEN>

# Copy and apply policy
kubectl cp vault-read.hcl vault/vault-0:/tmp/vault-read.hcl
kubectl exec -it -n vault vault-0 -- vault policy write vault-read /tmp/vault-read.hcl

# Create Kubernetes secret with Vault token
kubectl create secret generic vault-token -n external-secrets --from-literal=token=<root-token>
```

### Verify Vault Configuration

```bash
kubectl describe externalsecret <secret-name> -n <namespace>
kubectl describe ClusterSecretStore vault-backend
kubectl get externalsecrets -A
kubectl rollout restart deployment external-secrets -n external-secrets
```

---

## Database (CloudNativePG)

### Install Operator

```bash
kubectl apply --server-side -f \
  https://raw.githubusercontent.com/cloudnative-pg/cloudnative-pg/release-1.28/releases/cnpg-1.28.0.yaml
```

### Connect to a Database

```bash
# Port-forward
kubectl port-forward svc/catalog-db-rw 5432:5432 &

# List databases
kubectl exec -it catalog-db-1 -- psql -U postgres -c "\l"

# Direct connection
psql -h localhost -p 5432 -U <user> -d <database>
```

### Patch Persistent Volume Reclaim Policy

```bash
kubectl patch pv <pv-id> -p '{"spec":{"persistentVolumeReclaimPolicy":"Retain"}}'
```

### Patch Database Resource Limits

```bash
# auth-db
kubectl patch cluster auth-db --type merge -p \
  '{"spec":{"resources":{"requests":{"cpu":"100m","memory":"256Mi"},"limits":{"cpu":"500m","memory":"512Mi"}}}}'

# bid-db
kubectl patch cluster bid-db --type merge -p \
  '{"spec":{"resources":{"requests":{"cpu":"100m","memory":"256Mi"},"limits":{"cpu":"500m","memory":"512Mi"}}}}'

# catalog-db
kubectl patch cluster catalog-db --type merge -p \
  '{"spec":{"resources":{"requests":{"cpu":"100m","memory":"256Mi"},"limits":{"cpu":"500m","memory":"512Mi"}}}}'
```

---

## Ingress

### Install NGINX Ingress Controller

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

### Test Ingress

```bash
curl -v "http://app.local/ws/socket.io/?EIO=4&transport=polling"
```

### Install cert-manager

```bash
helm repo add jetstack https://charts.jetstack.io
helm repo update

kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.3/cert-manager.crds.yaml

helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.13.3
```

### Verify cert-manager

```bash
kubectl get clusterissuer
kubectl get certificate -n default
kubectl describe certificate app-local-tls -n default
kubectl get svc -n kube-system | grep traefik
```

---

### HTTPS with Let's Encrypt (cert-manager + Traefik)

This sets up automatic TLS for `server.nilami.click` using cert-manager and Let's Encrypt via the HTTP-01 challenge. Port 80 must be open for the challenge to work. Once done, the backend will be reachable at `https://server.nilami.click`.

**Step 1 — Install cert-manager**

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```

Wait until all three pods are running:

```bash
kubectl get pods -n cert-manager
```

**Step 2 — Create the ClusterIssuer**

The `ClusterIssuer` tells cert-manager how to communicate with Let's Encrypt. Create `issuer.yaml`:

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com  # Use a real email to receive renewal alerts
    privateKeySecretRef:
      name: letsencrypt-prod-key
    solvers:
    - http01:
        ingress:
          class: traefik
```

```bash
kubectl apply -f issuer.yaml
```

**Step 3 — Update the Ingress**

Add the cert-manager annotation and a `tls` block to `app-ingress`. cert-manager will automatically create and populate the TLS secret.

```bash
kubectl edit ingress app-ingress
```

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    kubernetes.io/ingress.class: "traefik"
    acme.cert-manager.io/http01-edit-in-place: "true"
spec:
  tls:
  - hosts:
    - server.nilami.click
    secretName: server-nilami-tls  # Created automatically by cert-manager
  rules:
  - host: server.nilami.click
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: api-gateway
            port:
              number: 8084
```

---

## Stripe

```bash
# Listen for events in development
stripe listen \
  --events payment_intent.succeeded,payment_intent.payment_failed \
  --forward-to http://app.local/api/v1/auth/payment/webhook

# Create a test payment intent
stripe payment_intents create \
  -d amount=2000 \
  -d currency=usd \
  -d "metadata[userId]=user123" \
  -d "automatic_payment_methods[enabled]=true" \
  -d "automatic_payment_methods[allow_redirects]=never"

# Confirm with a test card
stripe payment_intents confirm <payment_intent_id> --payment-method=pm_card_visa
```

---

## Monitoring *(not currently active)*

The monitoring stack uses Prometheus (metrics), Loki (logs), and Grafana (dashboards/alerting).

```bash
# Install stack
helm install monitoring grafana/loki-stack \
  --set prometheus.enabled=true \
  --set loki.enabled=true \
  --set grafana.enabled=true

# Upgrade Loki version
helm upgrade monitoring grafana/loki-stack \
  --set loki.image.tag=2.9.4 \
  --reuse-values

# Upgrade with custom values
helm upgrade monitoring grafana/loki-stack -f monitoring-values.yaml

# Restart monitoring deployments
kubectl rollout restart deployment -n monitoring

# Access Grafana (http://localhost:3000)
kubectl port-forward svc/monitoring-grafana 3000:80

# Get Grafana admin password
kubectl get secret monitoring-grafana -o jsonpath="{.data.admin-password}" | base64 --decode; echo
```

---

## Troubleshooting & Operations

### Quick Reference

| Task | Command |
|------|---------|
| Shell into a pod | `kubectl exec -it -n <namespace> <pod> -- bash` |
| Connect to database | `psql -h localhost -p 5432 -U <user> -d <database>` |
| Port-forward a service | `kubectl port-forward svc/<service> <local>:<remote>` |
| Stream logs | `kubectl logs -f <pod> -n <namespace>` |
| Get pod status | `kubectl get pods -n <namespace>` |
| Describe a resource | `kubectl describe <type> <name> -n <namespace>` |
| Watch pods | `kubectl get pods -w` |
| Restart a deployment | `kubectl rollout restart deployment/<name>` |
| Scale down a deployment | `kubectl scale deployment <name> --replicas=0` |
| View resource usage | `kubectl top pods --all-namespaces --sort-by=memory` |
| Clear kubectl cache | `rm -rf ~/.kube/cache ~/.kube/http-cache` |




