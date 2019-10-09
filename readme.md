# Multibanking

## Introduction
The multibanking project provides authenticated access to a users bank account. 

With validated online-banking credentials, the user can authorize an application to fetch transaction data, prepare and display them. The project itself uses direct HBCI.

## Prerequisites

- Installed docker and docker-compose.
- Locally available docker custom keycloak image for database encryption, see https://github.com/adorsys/keycloak-user-secret-adapter

## Building
 ```
 git clone https://github.com/adorsys/multibanking.git
 mvn clean install
  ```

## Running

 You can run the application from the command line using:
 
```
cd multibanking-server
docker-compose up
```

Then use http://localhost:8088 to see provided endpoints
