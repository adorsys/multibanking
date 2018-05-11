package de.adorsys.multibanking.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Key/Code zur Identifizierung der Message.
     */
    @ApiModelProperty(value = "The http status code", example = "401")
    private String key;

    private Severity severity;

    /**
     * Feldbezug bei Validierungsfehlern. Optional.
     */
    private String field;

    /**
     * Ausformulierte Beschreibung des Fehlers. Optional.
     */
    private String renderedMessage;

    /**
     * Zurätzliche Informationen zu dem Fehler- Optional.
     */
    private Map<String, String> paramsMap; // NOSONARz

    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    public Message(String key, Severity severity) {
        this.key = key;
        this.severity = severity;
    }

    public Message(String key, Severity severity, String renderedMessage) {
        this.key = key;
        this.severity = severity;
        this.renderedMessage = renderedMessage;
    }

    public Message(String key, Severity severity, String renderedMessage, Map<String, String> paramsMap) {
        this.key = key;
        this.severity = severity;
        this.renderedMessage = renderedMessage;
        this.paramsMap = paramsMap;
    }
}
