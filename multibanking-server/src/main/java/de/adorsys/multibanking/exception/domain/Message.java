package de.adorsys.multibanking.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Message implements Serializable {

    private static final long serialVersionUID = -1L;

    private String key;

    private Severity severity;

    private String field;

    private String renderedMessage;

    private Map<String, String> paramsMap;

    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }
}
