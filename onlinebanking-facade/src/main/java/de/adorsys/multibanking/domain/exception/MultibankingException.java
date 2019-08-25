package de.adorsys.multibanking.domain.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultibankingException extends RuntimeException {

    private List<Message> messages;
    private MultibankingError multibankingError;

    public MultibankingException(MultibankingError multibankingError) {
        this(multibankingError, Collections.emptyList());
    }

    public MultibankingException(MultibankingError multibankingError, String messageString) {
        this(multibankingError, Collections.singletonList(Message.builder()
            .renderedMessage(messageString)
            .build())
        );
    }

    public MultibankingException(MultibankingError multibankingError, Message message) {
        this(multibankingError, Collections.singletonList(message));
    }

    public MultibankingException(MultibankingError multibankingError, List<Message> messages) {
        super(messages.toString());
        this.messages = messages;
        this.multibankingError = multibankingError;
    }

}
