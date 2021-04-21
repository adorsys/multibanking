package de.adorsys.multibanking.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.stream.Collectors;

import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.OAUTH2;

@RequiredArgsConstructor
@Configuration
@Profile("swagger")
public class SwaggerConfig {

    @Value("${idp.baseUrl}")
    private String idpBaseUrl;
    @Value("${info.project.version}")
    private String version;
    @Value("${idp.realm:multibanking}")
    private String realm;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("Multibanking Rest API")
                .version(version)
                .contact(new Contact().name("Alexander Geist").email("age@adorsys.de").url("https://www.adorsys.de"))
                .license(new License()))
            .externalDocs(new ExternalDocumentation()
                .url("https://github.com/adorsys/multibanking"))
            .schemaRequirement("multibanking_auth", securityScheme());
    }

    @Bean
    public GroupedOpenApi multibankingApi() {
        return GroupedOpenApi.builder()
            .setGroup("multibanking")
            .packagesToScan("de.adorsys.multibanking.web")
            .addOpenApiCustomiser(sortPathsAlphabetically())
            .build();
    }

    @Bean
    @Profile("smartanalytics-embedded")
    public GroupedOpenApi smartanalyticsApi() {
        return GroupedOpenApi.builder()
            .setGroup("smartanalytics")
            .packagesToScan("de.adorsys.smartanalytics.web")
            .addOpenApiCustomiser(sortPathsAlphabetically())
            .build();
    }

    @Bean
    public OpenApiCustomiser sortPathsAlphabetically() {
        return openApi -> openApi.setPaths(openApi.getPaths()
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, Paths::new)));
    }

    private SecurityScheme securityScheme() {
        String tokenEndpoint = String.format("%s/auth/realms/%s/protocol/openid-connect/token", idpBaseUrl, realm);
        String authEndpoint = String.format("%s/auth/realms/%s/protocol/openid-connect/auth", idpBaseUrl,
            realm);
        return new SecurityScheme()
            .type(OAUTH2)
            .in(HEADER)
            .bearerFormat("jwt")
            .flows(new OAuthFlows()
                .authorizationCode(new OAuthFlow()
                    .tokenUrl(tokenEndpoint)
                    .authorizationUrl(authEndpoint)));
    }

}
