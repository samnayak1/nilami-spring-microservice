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


eval $(minikube docker-env)
Build the Image: Build your image again (or ensure it's built). The image will be built directly into Minikube's accessible cache.

Bash
docker image list
docker build -t catalogbackend:latest .
Reset Docker Environment: Switch back to your host's Docker daemon.

Bash

eval $(minikube docker-env -u)


kubectl rollout restart deployment/catalog-server

 kubectl get pods --watch
 kubectl get pods -o wide

kubectl exec -it catalog-server-5979d49f96-r7jlp -- curl -v http://registry-service:8761/eureka



kubectl describe pod <podname>



 unsealed

 apiVersion: v1
kind: Secret
metadata:
  name: catalog-db-secret
type: Opaque
data:
  POSTGRES_USER: <base64-encoded-POSTGRES_USER>
  POSTGRES_PASSWORD: <base64-encoded-POSTGRES_PWD>
  POSTGRES_DB: <base64-encoded-POSTGRES_DB>


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



# Delete the temporary file with plaintext
rm kafka-secret-temp.yaml



helm install kafka bitnami/kafka -f kafka-values.yaml --namespace <namespace> --create-namespace


helm uninstall kafka --namespace kafka


helm upgrade kafka bitnami/kafka \
  -f kafka-values.yaml \
  --namespace kafka


  kubectl exec websocket-service-deployment-6f9b65bb6f-m5m58 -- printenv | 
grep KAFKA_BROKER