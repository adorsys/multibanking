# Prequisites

 * running mongodb instance

# Build Instructions

 * git build and clone multibanking https://github.com/adorsys/multibanking
 * build multibanking-parent `mvn clean install -f multibanking-parent/pom.xml`
 * build multibanking-service `mvn clean install -f multibanking-service/pom.xml`

 You can run the application from the command line using:
```
mvn spring-boot:run
```

# REST API

will be generated during built process
multibanking-service/target/generated-docs/api-guide.html