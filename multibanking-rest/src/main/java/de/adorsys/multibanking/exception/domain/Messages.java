package de.adorsys.multibanking.exception.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static de.adorsys.multibanking.exception.domain.Message.Severity.ERROR;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Messages implements Serializable {

    private static final long serialVersionUID = -1L;

    private String uuid;

    @Singular
    private Collection<Message> messages;

    public static Messages createError(String key) {
        return builder()
                .message(Message.builder()
                        .key(key)
                        .severity(ERROR)
                        .build())
                .build();
    }

    public static Messages createError(String key, String renderedMessage) {
        return builder()
                .message(Message.builder()
                        .key(key)
                        .severity(ERROR)
                        .renderedMessage(renderedMessage)
                        .build())
                .build();
    }

    public static Messages createError(String key, String renderedMessage, Map<String, String> params) {
        return builder()
                .message(Message.builder()
                        .key(key)
                        .severity(ERROR)
                        .renderedMessage(renderedMessage)
                        .paramsMap(params)
                        .build())
                .build();
    }
}
