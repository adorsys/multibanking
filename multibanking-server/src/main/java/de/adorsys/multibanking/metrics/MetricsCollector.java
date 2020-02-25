package de.adorsys.multibanking.metrics;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    public void count(String tag, String bankCode, BankApi bankApi) {
        count(tag, bankCode, bankApi, null);
    }

    public void count(String tag, String bankCode, BankApi bankApi, Throwable exception) {
        count(tag, bankCode, bankApi, exception, null);
    }

    public void count(String tag, String bankCode, BankApi bankApi, Throwable exception, String tanMedia) {
        List<Tag> tagList = new ArrayList<>(Arrays.asList(Tag.of("bank_code", bankCode),
            Tag.of("bank_group", Bankgruppe.tagByBankCode(bankCode)),
            Tag.of("bank_api", bankApi == null ? "undefined" : bankApi.toString()),
            Tag.of("outcome", exception == null ? "SUCCESS" : "ERROR"),
            getExceptionTag(exception),
            tanMedia != null ? Tag.of("tan_media", tanMedia) : null
        ));
        tagList.removeIf(Objects::isNull);
        meterRegistry.counter(tag, Tags.of(tagList)).increment();
    }

    public void time(String tag, String bankCode, BankApi bankApi, Throwable throwable, long duration) {
        Timer.builder(tag)
            .tag("bank_code", bankCode)
            .tag("bank_group", Bankgruppe.tagByBankCode(bankCode))
            .tag("bank_api", bankApi == null ? "undefined" : bankApi.toString())
            .tag("outcome", throwable == null ? "SUCCESS" : "ERROR")
            .tag("exception", getExceptionTag(throwable).getValue())
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }

    private Tag getExceptionTag(Throwable exception) {
        if (exception instanceof MultibankingException) {
            return Tag.of("exception", ((MultibankingException) exception).getMultibankingError().toString());
        }
        return WebMvcTags.exception(exception);
    }
}
