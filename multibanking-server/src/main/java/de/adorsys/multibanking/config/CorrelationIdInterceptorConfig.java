package de.adorsys.multibanking.config;

import de.adorsys.multibanking.correlation.CorrelationIdInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AllArgsConstructor
@Configuration
public class CorrelationIdInterceptorConfig implements WebMvcConfigurer {

    private final CorrelationIdInterceptor correlationIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(correlationIdInterceptor);
    }
}
