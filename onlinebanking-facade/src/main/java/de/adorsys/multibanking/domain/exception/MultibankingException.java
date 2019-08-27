package de.adorsys.multibanking.domain.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultibankingException extends RuntimeException {

    private int httpResponseCode;
    private List<Message> messages;
    private MultibankingError multibankingError;

    public MultibankingException(MultibankingError multibankingError) {
        this(multibankingError, 400, Collections.emptyList());
    }

    public MultibankingException(MultibankingError multibankingError, int httpResponseCode, String messageString) {
        this(multibankingError, httpResponseCode, Collections.singletonList(Message.builder()
            .renderedMessage(messageString)
            .build())
        );
    }

    public MultibankingException(MultibankingError multibankingError, String messageString) {
        this(multibankingError, 400, Collections.singletonList(Message.builder()
            .renderedMessage(messageString)
            .build())
        );
    }

    public MultibankingException(MultibankingError multibankingError, List<Message> messages) {
        this(multibankingError, 400, messages);
    }

    public MultibankingException(MultibankingError multibankingError, int httpResponseCode, List<Message> messages) {
        super(messages.toString());
        this.messages = messages;
        this.multibankingError = multibankingError;
        this.httpResponseCode = httpResponseCode;
    }

}
