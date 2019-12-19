package de.adorsys.multibanking.metrics;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    public void count(String tag, String bankCode, BankApi bankApi) {
        count(tag, bankCode, bankApi, null);
    }

    public void count(String tag, String bankCode, BankApi bankApi, Throwable exception) {
        meterRegistry.counter(tag, Tags.of(Tag.of("bank_code", bankCode),
            Tag.of("bank_api", bankApi == null ? "undefined" : bankApi.toString()),
            Tag.of("outcome", exception == null ? "SUCCESS" : "ERROR"),
            getExceptionTag(exception))
        ).increment();
    }

    private Tag getExceptionTag(Throwable exception) {
        if (exception instanceof MultibankingException) {
            return Tag.of("exception", ((MultibankingException) exception).getMultibankingError().toString());
        }
        return WebMvcTags.exception(exception);
    }
}
