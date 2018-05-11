# Multibanking

# THIS BRANCH (smartanalytics-integration) REQUIRES to checkout and build https://github.com/adorsys/smartanalytics branch analytics-facade
# Still to be merged to develop.


## Introduction
The multibanking project provides authenticated access to a users bank account. With validated online-banking credentials, the user can authorize an application to fetch transaction data, prepare and display it. The project itself uses HBCI and Figo to do so. Furthermore, it is possible to test the functionality of this project not only with real- but with fake-data (mocks). The mocked-data can be set up by the user and acts just as real data. 


## Getting Started

### Prequisites

hbci4java fork: 
 ```
 git clone https://github.com/tadschik/hbci4java.git
 mvn clean install -f hbci4java/pom.xml
 ```
running mongodb instance:

### Build Instructions
 ```
 git clone https://github.com/adorsys/multibanking.git
 mvn clean install -f multibanking/multibanking-parent/pom.xml
 mvn clean install -f multibanking/onlinebanking-adapter/pom.xml
 mvn clean install -f multibanking/multibanking-persistence/pom.xml
 mvn clean install -f multibanking/multibanking-service/pom.xml
 ```


 You can run the application from the command line using:
```
mvn spring-boot:run -f multibanking/multibanking-service/pom.xml
```
Data will be cached at runtime within inmemory database fongo.

For storing data in a mongodb database run the application using command line:
```
mvn spring-boot:run -f multibanking/multibanking-service/pom.xml -Drun.profiles=mongo
```

multibanking-service is listening on port 10021

### REST API

will be generated during built process
multibanking/multibanking-service/target/generated-docs/api-guide.html

### SAMPLE CLIENT

 ```
 npm install -g cordova ionic
 cd multibanking-app/
 ionic serve
 ```
