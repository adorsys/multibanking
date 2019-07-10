package de.adorsys.multibanking.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

@Component
public class SwaggerOkStatusCodeFilteringPlugin implements OperationBuilderPlugin {

    @Override
    public void apply(OperationContext operationContext) {
        if (!operationContext.httpMethod().equals(HttpMethod.GET) && !operationContext.httpMethod().equals(HttpMethod.PUT)) {
            operationContext
                .operationBuilder()
                .build()
                .getResponseMessages()
                .removeIf(responseMessage -> responseMessage.getCode() == HttpStatus.OK.value());
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
