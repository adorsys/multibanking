# Multibanking

## Introduction
The multibanking project provides authenticated access to a users bank account. 

With validated online-banking credentials, the user can authorize an application to fetch transaction data, prepare and display them. The project itself uses direct HBCI to do so, but can be configured to use figo or finAPI. Furthermore, it is possible to test the functionality of this project not only with real- but with fake-data (mocks). The mocked-data can be set up by the user and acts just as real data. 

## Building
 ```
 git clone https://github.com/adorsys/multibanking.git
 mvn clean install
  ```

## Running

 You can run the application from the command line using:
 
```
mvn spring-boot:run -f multibanking-examples/multibanking-service-example/pom.xml
```

Then use http://loaclhost:8080 to see provided endpoints
