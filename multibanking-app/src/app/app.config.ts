export const AppConfig = {
  production: false,
  // Openshift
  // auth_url: 'https://multibanking-keycloak.cloud.adorsys.de/auth',
  // api_url: 'https://multibanking-service.cloud.adorsys.de/api/v1'

  // multibanking service running from docker container
  // auth_url: 'http://keycloak:8080/auth',
  // api_url: 'http://localhost:8081/api/v1'

  // multibanking service running from ide
  auth_url: 'http://localhost:8080/auth',
  api_url: 'http://localhost:8081/api/v1'
};
