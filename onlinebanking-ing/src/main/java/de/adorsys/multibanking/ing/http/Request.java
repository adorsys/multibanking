package de.adorsys.multibanking.ing.http;

import de.adorsys.multibanking.ing.http.HttpClient;
import de.adorsys.multibanking.ing.model.Response;

import java.util.Map;
import java.util.function.UnaryOperator;

public interface Request {

    interface Builder {

        String method();

        String uri();

        Builder jsonBody(String body);

        String jsonBody();

        Builder emptyBody(boolean empty);

        boolean emptyBody();

        Builder urlEncodedBody(Map<String, String> formData);

        Map<String, String> urlEncodedBody();

        Builder headers(Map<String, String> headers);

        Map<String, String> headers();

        Builder header(String name, String value);

        <T> Response<T> send(Interceptor interceptor, HttpClient.ResponseHandler<T> responseHandler);

        default <T> Response<T> send(HttpClient.ResponseHandler<T> responseHandler) {
            return send(x -> x, responseHandler);
        }

        String content();

        @FunctionalInterface
        interface Interceptor extends UnaryOperator<Builder> {
        }
    }
}
