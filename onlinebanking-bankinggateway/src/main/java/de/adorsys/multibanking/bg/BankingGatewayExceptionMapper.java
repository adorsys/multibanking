package de.adorsys.multibanking.bg;

import com.google.gson.JsonSyntaxException;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.xs2a_adapter.ApiException;
import de.adorsys.multibanking.xs2a_adapter.model.Error400NGAIS;
import de.adorsys.multibanking.xs2a_adapter.model.TppMessage400AIS;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

public class BankingGatewayExceptionMapper {
    private static BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

    public static MultibankingException toMultibankingException(ApiException e, MultibankingError multibankingError) {
        try {
            Error400NGAIS messagesTO = GsonConfig.getGson().fromJson(e.getResponseBody(),
                Error400NGAIS.class);
            return new MultibankingException(multibankingError, e.getCode(), null,
                bankingGatewayMapper.toMessagesFromTppMessage400AIS(messagesTO.getTppMessages()));
        } catch (JsonSyntaxException jpe) {
            // try xml
            TppMessage400AIS messageTO = Optional.ofNullable(e.getResponseBody())
                .filter(xml -> xml.startsWith("<"))
                .map(uncheckFunction(XML::toJSONObject))
                .map(BankingGatewayExceptionMapper::findTppMessage)
                .map(uncheckFunction(message -> GsonConfig.getGson().fromJson(message.toString(), TppMessage400AIS.class)))
                .orElse(null);

            if (messageTO != null) {
                return new MultibankingException(multibankingError, e.getCode(), null,
                    Collections.singletonList(bankingGatewayMapper.toMessage(messageTO)));
            } else {
                return new MultibankingException(multibankingError, 500, e.getMessage());
            }
        } catch (Exception e2) {
            return new MultibankingException(multibankingError, 500, e.getMessage());
        }
    }

    private static JSONObject findTppMessage(JSONObject jsonObject) {
        JSONObject message = null;
        for (Iterator key = jsonObject.keys(); key.hasNext(); ) {
            String nextKey = (String) key.next();
            Object next = jsonObject.get(nextKey);
            if ("tppMessages".equals(nextKey)) {
                message = (JSONObject) next;
            }
            if (next instanceof JSONObject) {
                JSONObject override = findTppMessage((JSONObject) next);
                message = override != null ? override : message;
            }
        }
        return message;
    }

    static <T, R> Function<T, R> uncheckFunction(CheckedFunction<T, R> throwingFunction) {
        return i -> {
            try {
                return throwingFunction.apply(i);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        };
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws IOException;
    }
}
