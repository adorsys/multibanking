package de.adorsys.multibanking.config;

import de.adorsys.multibanking.domain.exception.MultibankingException;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.metrics.web.servlet.DefaultWebMvcTagsProvider;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class MetricsConfig {

    @Bean
    public WebMvcTagsProvider webMvcTagsProvider() {
        return new CustomWebMvcTagsProvider();
    }

    private static class CustomWebMvcTagsProvider extends DefaultWebMvcTagsProvider {
        @Override
        public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler,
                                     Throwable exception) {
            return Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, response),
                getExceptionTag(exception), WebMvcTags.status(response), WebMvcTags.outcome(response));
        }

        private Tag getExceptionTag(Throwable exception) {
            if (exception instanceof MultibankingException) {
                return Tag.of("exception", ((MultibankingException) exception).getMultibankingError().toString());
            }
            return WebMvcTags.exception(exception);
        }
    }

}
