package de.adorsys.multibanking.config;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import de.adorsys.multibanking.web.UserResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseEntity;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableSwagger2
@Profile("swagger")
public class SwaggerConfig {

    private final Environment environment;

    @Value("${idp.baseUrl}")
    private String loginUrl;
    @Value("${info.project.version}")
    private String version;
    @Value("${swagger.client.id:multibanking-client}")
    private String swaggerClientId;
    @Value("${idp.realm:multibanking}")
    private String realm;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .apis(apis())
            .paths(PathSelectors.any())
            .build()
            .useDefaultResponseMessages(false)
            .directModelSubstitute(ResponseEntity.class, java.lang.Void.class)
            .securitySchemes(Collections.singletonList(securityScheme()))
            .securityContexts(Collections.singletonList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Multibanking REST Api")
            .description("Use a bank code (blz) ending with X00 000 00 like 300 000 00 to"
                + " run aggainst the mock backend. Find the mock backend at ${hostname}:10010")
            .contact(new Contact("Alexander Geist adorsys GmbH & Co. KG", null, "age@adorsys.de"))
            .version(version)
            .build();
    }

    private Predicate<RequestHandler> apis() {
        Predicate<RequestHandler> mbResourses = RequestHandlerSelectors.withClassAnnotation(UserResource.class);

        if (environment.acceptsProfiles(Profiles.of("smartanalytics-embedded"))) {
            return Predicates.or(mbResourses, RequestHandlerSelectors.basePackage("de.adorsys.smartanalytics.web"));
        } else {
            return mbResourses;
        }
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(Collections.singletonList(new SecurityReference("multibanking_auth", scopes())))
            .forPaths(PathSelectors.any())
            .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
            .clientId(swaggerClientId)
            .build();
    }

    private SecurityScheme securityScheme() {
        String tokenEndpoint = String.format("%s/auth/realms/%s/protocol/openid-connect/token", loginUrl, realm);
        String tokenRequestEndpoint = String.format("%s/auth/realms/%s/protocol/openid-connect/auth", loginUrl, realm);

        GrantType grantType = new AuthorizationCodeGrantBuilder()
            .tokenEndpoint(new TokenEndpoint(tokenEndpoint, "token"))
            .tokenRequestEndpoint(new TokenRequestEndpoint(tokenRequestEndpoint, swaggerClientId, null))
            .build();

        return new OAuthBuilder()
            .name("multibanking_auth")
            .grantTypes(Collections.singletonList(grantType))
            .scopes(Arrays.asList(scopes()))
            .build();
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[]{new AuthorizationScope("openid", "openid connect")};
    }
}
