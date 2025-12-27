# nilami-spring-microservice
nilami with microservices




To run tests
mvn -Dtest=ItemServiceTest test
mvn -Dtest=CategoryServiceTest test


To create a new service
curl https://start.spring.io/starter.zip       -d language=java     -d type=maven-project     -d groupId=com.nilami     -d artifactId=registry-service     -d name=registry-service     -d packageName=com.nilami.registryservice     -d javaVersion=21     -o registry-service.zip




To install
1. Docker
2. Minikube
3. Helm
4. Kubeseal
minikube start \
  --driver=docker \
  --memory=4096 \
  --cpus=2 \
  --disk-size=25g


docker build -t <imagename>:latest .
docker tag <imagename>:latest <dockerhub-username>/<imagename>:<version>
docker push <dockerhub-username>/<image name>:<version>


OR
eval $(minikube docker-env)
Build the Image: Build your image again (or ensure it's built). The image will be built directly into Minikube's accessible cache.

Bash
docker image list

Reset Docker Environment: Switch back to your host's Docker daemon.

Bash

eval $(minikube docker-env -u)


kubectl rollout restart deployment/catalog-server

 kubectl get pods --watch
 kubectl get pods -o wide

kubectl exec -it catalog-server-5979d49f96-r7jlp -- curl -v http://registry-service:8761/eureka



kubectl describe pod <podname>

to view by memory
minikube addons enable metrics-server
 kubectl top pods --all-namespaces --sort-by=memory
 
 unsealed

 apiVersion: v1
kind: Secret
metadata:
  name: catalog-secret
type: Opaque
data:
  POSTGRES_USER: <base64-encoded-POSTGRES_USER>
  POSTGRES_PASSWORD: <base64-encoded-POSTGRES_PWD>
  POSTGRES_DB: <base64-encoded-POSTGRES_DB>
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/latest/download/controller.yaml
kubectl get crd | grep sealed

kubeseal --format=yaml < catalog-secret-unsealed.yaml --controller-namespace kube-system \
  --controller-name sealed-secrets > catalog-secret-sealed.yaml

kubectl apply -f catalog-secret-sealed.yaml


echo -n "secret" | base64


KAFKA_ADMIN_PASSWORD=$(openssl rand -base64 32)
KAFKA_USER1_PASSWORD=$(openssl rand -base64 32)
KAFKA_CONTROLLER_PASSWORD=$(openssl rand -base64 32)

echo "Admin: $KAFKA_ADMIN_PASSWORD"
echo "User1: $KAFKA_USER1_PASSWORD"
echo "Controller: $KAFKA_CONTROLLER_PASSWORD"

kubectl create secret generic kafka-user-passwords \
  --from-literal=client-passwords="$KAFKA_USER1_PASSWORD" \
  --from-literal=system-user-password="$KAFKA_ADMIN_PASSWORD" \
  --from-literal=controller-password="$KAFKA_CONTROLLER_PASSWORD" \
  --namespace kafka \
  --dry-run=client -o yaml > kafka-secret-temp.yaml


for hashicorp

helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

helm show chart hashicorp/vault

helm install vault hashicorp/vault \
  --namespace vault \
  --create-namespace \
  --set server.dev.enabled=true

  # for prod
  helm install vault hashicorp/vault \
  -n vault --create-namespace \
  -f vault-values.yaml

helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets \
  --create-namespace

kubectl get pods -n vault

kubectl port-forward -n vault svc/vault 8200:8200

Token: root in dev

kubectl exec -n vault -it vault-0 -- sh
kubectl create secret generic vault-token \
  --namespace external-secrets \
  --from-literal=token=<token>
kubectl exec -it -n vault vault-0 -- vault login <ROOT_TOKEN>
vault operator init

kubectl cp vault-read.hcl vault/vault-0:/tmp/vault-read.hcl
kubectl exec -it -n vault vault-0 -- vault policy write vault-read /tmp/vault-read.hcl

check if sealed
kubectl exec -n vault vault-0 -- vault status

kubectl exec -n vault vault-0 -- vault operator unseal <key> //with any three keys out of the 5 if threshold is 3

kubectl get secret kafka-consumer-credentials -o jsonpath='{.data.password}' | base64 -d


to force a sync
kubectl annotate externalsecret kafka-consumer-credentials force-sync=$(date +%s) --overwrite

 vault  kv put secret/bid-service KAFKA_PASSWORD=example
# adding external secret
helm repo add external-secrets https://charts.external-secrets.io
helm repo update


  
kubectl create secret generic vault-token \
  -n external-secrets \
  --from-literal=token=YOUR_VAULT_TOKEN

kubectl get externalsecret kafka-consumer-credentials

kubectl describe externalsecret kafka-consumer-credentials

kubectl describe clustersecretstore vault-backend
if you get a SecretSyncedError
kubectl rollout restart deployment -n external-secrets

kubectl logs vault-0 -n vault


minikube addons enable storage-provisioner
minikube addons enable default-storageclass
# create secret



# Delete the temporary file with plaintext
rm kafka-secret-temp.yaml



helm install kafka bitnami/kafka -f kafka-values.yaml --namespace <namespace> --create-namespace


helm uninstall kafka --namespace kafka


helm upgrade kafka bitnami/kafka \
  -f kafka-values.yaml \
  --namespace kafka


  kubectl exec websocket-service-deployment-6f9b65bb6f-m5m58 -- printenv | 
grep KAFKA_BROKER


kubectl scale deployment <deployment-name> --replicas=0



if minikube does not work
sudo ip link set dev eth0 mtu 1350


clear cache kubectl
rm -rf ~/.kube/cache ~/.kube/http-cache



# cnpg


kubectl apply --server-side -f \
  https://raw.githubusercontent.com/cloudnative-pg/cloudnative-pg/release-1.28/releases/cnpg-1.28.0.yaml
kubectl rollout status deployment \
  -n cnpg-system cnpg-controller-manager

kubectl get deployments -n cnpg-system

 to connect
catalog-db-rw

it automatically generates secrets
 kubectl get secrets

 to head into the database

 # Port forward if not already running
kubectl port-forward svc/catalog-db-rw 5432:5432 &

# Connect as superuser and check if the user exists
kubectl exec -it catalog-db-1 -- psql -U postgres -c "\du"

# Check what databases exist
kubectl exec -it catalog-db-1 -- psql -U postgres -c "\l"