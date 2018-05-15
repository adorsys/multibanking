package de.adorsys.multibanking.config.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.adorsys.multibanking.web.annotation.UserResource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alexg on 10.03.17.
 */
@Configuration
@EnableSwagger2
@Profile("swagger")
public class SwaggerConfig {

    @Value("${keycloak_AUTH_SERVER_ISS_URL:http://localhost:8080/auth/realms/multibanking}")
    String authUrl;
    @Value("${info.project.version}")
    String version;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Multibanking REST Api")
                        .description("Use a bank code (blz) ending with X00 000 00 like 300 000 00 to"
                                + " run aggainst the mock backend. Find the mock backend at ${hostname}:10010")
                        .contact(new Contact("Alexander Geist adorsys GmbH & Co. KG", null, "age@adorsys.de"))
                        .version(version)
                        .build())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(UserResource.class))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(securitySchema()));
    }

    private OAuth securitySchema() {
        List<AuthorizationScope> authorizationScopeList = new ArrayList<>();

        List<GrantType> grantTypes = new ArrayList<>();
        GrantType grantType = new ResourceOwnerPasswordCredentialsGrant(authUrl+"/protocol/openid-connect/token");
        grantTypes.add(grantType);

        return new OAuthBuilder()
                .name("oauth2")
                .grantTypes(grantTypes)
                .scopes(authorizationScopeList)
                .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return new SecurityConfiguration(
                "multibanking-client", "foo", "multibanking", "foo", "foo", ApiKeyVehicle.HEADER, "api_key", ",");
    }
}
