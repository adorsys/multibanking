## Prequisites

###bci4java fork 
 ```
 git clone https://github.com/tadschik/hbci4java.git
 mvn clean install -f hbci4java/pom.xml
 ```
###running mongodb instance

## Build Instructions

 ```
 git clone https://github.com/adorsys/multibanking.git
 mvn clean install -f multibanking/multibanking-parent/pom.xml
 mvn clean install -f multibanking/onlinebanking-adapter/pom.xml
 mvn clean install -f multibanking/multibanking-service/pom.xml
 ```


 You can run the application from the command line using:
```
mvn spring-boot:run -f multibanking/multibanking-service/pom.xml
```

multibanking-service is listening on port 10021

## REST API

will be generated during built process
multibanking/multibanking-service/target/generated-docs/api-guide.html

## SAMPLE CLIENT

 ```
 npm install -g cordova ionic
 cd multibanking-app/
 ionic serve
 ```