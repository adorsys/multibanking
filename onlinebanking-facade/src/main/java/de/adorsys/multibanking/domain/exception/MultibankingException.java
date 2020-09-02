package de.adorsys.multibanking.domain.exception;

import de.adorsys.multibanking.domain.PsuMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultibankingException extends RuntimeException {

    private final int httpResponseCode;
    private final List<PsuMessage> psuMessages;
    private final List<Message> tppMessages;
    private final MultibankingError multibankingError;

    public MultibankingException(MultibankingError multibankingError) {
        this(multibankingError, 400, Collections.emptyList(), null);
    }

    public MultibankingException(MultibankingError multibankingError, int httpResponseCode, String psuMessage) {
        this(multibankingError, httpResponseCode, Collections.singletonList(new PsuMessage(null, psuMessage)), null
        );
    }

    public MultibankingException(MultibankingError multibankingError, String psuMessage) {
        this(multibankingError, 400, Collections.singletonList(new PsuMessage(null, psuMessage)), null
        );
    }

    public MultibankingException(MultibankingError multibankingError, List<PsuMessage> psuMessages) {
        this(multibankingError, 400, psuMessages, null);
    }

    public MultibankingException(MultibankingError multibankingError, int httpResponseCode, List<PsuMessage> psuMessages, List<Message> tppMessages) {
        super(toExceptionString(psuMessages, tppMessages));
        this.psuMessages = psuMessages;
        this.multibankingError = multibankingError;
        this.httpResponseCode = httpResponseCode;
        this.tppMessages = tppMessages;
    }

    @Override
    public String toString() {
        return toExceptionString(psuMessages, tppMessages);
    }

    private static String toExceptionString(List<PsuMessage> psuMessages, List<Message> tppMessages) {
        StringBuilder stringBuilder = new StringBuilder();
        if (psuMessages != null) {
            psuMessages.forEach(psuMessage -> stringBuilder.append(psuMessage.toString()).append("\\n"));
        }
        if (tppMessages != null) {
            tppMessages.forEach(tppMessage -> stringBuilder.append(tppMessage.toString()).append("\\n"));
        }
        return stringBuilder.toString();
    }

}
