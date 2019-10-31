package de.adorsys.multibanking.bg;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Optional;

import static de.adorsys.multibanking.bg.Constants.CORRELATION_ID;
import static de.adorsys.multibanking.bg.Constants.CORRELATION_ID_HEADER;

public class OkHttpCorrelationIdInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = Optional.ofNullable(MDC.get(CORRELATION_ID))
            .map(correlationId -> {
                Request original = chain.request();
                return original.newBuilder()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .method(original.method(), original.body())
                    .build();
            }).orElse(chain.request());

        return chain.proceed(request);
    }
}
