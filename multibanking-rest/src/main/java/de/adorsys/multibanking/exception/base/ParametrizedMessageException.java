package de.adorsys.multibanking.exception.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ParametrizedMessageException extends RuntimeException {
    
    private final Map<String, String> paramsMap = new HashMap<>();

    public ParametrizedMessageException() {
    }

    public ParametrizedMessageException(String message) {
        super(message);
    }

    protected void addParam(String key, String value) {
        this.paramsMap.put(key, value);
    }

}
