package de.adorsys.multibanking.bg;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.xs2a.adapter.service.model.ErrorResponse;
import de.adorsys.xs2a.adapter.service.model.TppMessage;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class FeignTppMessageDecoder implements ErrorDecoder {

    ObjectMapper objectMapper = new ObjectMapper();
    BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

    @Override
    public Exception decode(String methodKey, Response response) {
        String bodyString = Optional.ofNullable(response)
            .map(Response::body)
            .map(body -> uncheckCall(body::asInputStream))
            .map(uncheckFunction(stream -> IOUtils.toString(stream)))
            .map(String::trim)
            .orElse(null);

        // xml
        TppMessage tppMessage = Optional.ofNullable(bodyString)
            .filter(xml -> xml.startsWith("<"))
            .map(uncheckFunction(string -> XML.toJSONObject(string)))
            .map(jsonObject -> walk(jsonObject))
            .map(uncheckFunction(message -> objectMapper.readValue(message.toString(), TppMessage.class)))
            .orElse(null);

        // json
        if (tppMessage == null) {
            tppMessage = Optional.ofNullable(bodyString)
                .map(uncheckFunction(json -> objectMapper.readValue(bodyString, ErrorResponse.class)))
                .map(ErrorResponse::getTppMessages)
                .map(list -> list.isEmpty() ? null : list.get(0))
                .orElse(null);
        }

        return new MultibankingException(MultibankingError.XS2A_ERROR, response.status(), Arrays.asList(bankingGatewayMapper.toMessage(tppMessage)));
    }

    private static JSONObject walk(JSONObject jsonObject) {
        JSONObject message = null;
        for (Iterator key = jsonObject.keys(); key.hasNext();) {
            String nextKey = (String) key.next();
            Object next = jsonObject.get(nextKey);
            if ("tppMessages".equals(nextKey)) {
                message = (JSONObject) next;
            }
            if (next instanceof JSONObject) {
                JSONObject override = walk((JSONObject) next);
                message = override != null ? override : message;
            }
        }
        return message;
    }

    private static <T> T uncheckCall(Callable<T> callable) {
        try { return callable.call(); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static <T, R> Function<T, R> uncheckFunction(CheckedFunction<T, R> throwingFunction) {
        return i -> {
            try {
                return throwingFunction.apply(i);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws IOException;
    }
}
