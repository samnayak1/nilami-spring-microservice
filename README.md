# nilami-spring-microservice
nilami with microservices




To run tests
mvn -Dtest=ItemServiceTest test
mvn -Dtest=CategoryServiceTest test


To create a new service
curl https://start.spring.io/starter.zip       -d language=java     -d type=maven-project     -d groupId=com.nilami     -d artifactId=registry-service     -d name=registry-service     -d packageName=com.nilami.registryservice     -d javaVersion=21     -o registry-service.zip
