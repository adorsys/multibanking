package de.adorsys.multibanking.config;

import de.adorsys.multibanking.logging.LoggingRestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class RestInterceptorConfig implements WebMvcConfigurer {

    private final LoggingRestInterceptor loggingRestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingRestInterceptor);
    }

}
