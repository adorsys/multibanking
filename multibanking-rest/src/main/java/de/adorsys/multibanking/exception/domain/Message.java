package de.adorsys.multibanking.exception.domain;

import io.swagger.annotations.ApiModelProperty;
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
}

