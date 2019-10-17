package de.adorsys.multibanking.correlation;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OkHttpCorrelationIdInterceptor implements Interceptor {
    @NonNull
    private final CorrelationId correlationId;

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (StringUtils.isEmpty(correlationId.getCorrelationId())) {
            return chain.proceed(chain.request());
        }

        Request original = chain.request();

        Request request = original.newBuilder()
                .header(CorrelationId.CORRELATION_ID, correlationId.getCorrelationId())
                .method(original.method(), original.body())
                .build();

        return chain.proceed(request);
    }
}
