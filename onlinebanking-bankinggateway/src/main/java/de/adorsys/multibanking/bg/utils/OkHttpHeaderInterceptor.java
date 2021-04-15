package de.adorsys.multibanking.bg.utils;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Optional;

import static de.adorsys.multibanking.bg.utils.Constants.*;

@RequiredArgsConstructor
public class OkHttpHeaderInterceptor implements Interceptor {

    private final String accessToken;

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = Optional.ofNullable(MDC.get(CORRELATION_ID))
            .map(correlationId -> chain.request().newBuilder()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build())
            .orElse(chain.request());

        return chain.proceed(Optional.ofNullable(accessToken)
            .map(authorization -> request.newBuilder()
                .header(AUTHORIZATION_HEADER, "Bearer " + authorization)
                .build())
            .orElse(request));
    }
}
