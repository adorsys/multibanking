package de.adorsys.multibanking.exception;

import java.util.HashMap;
import java.util.Map;

public class ParametrizedMessageException extends RuntimeException {
    private final Map<String, String> paramsMap = new HashMap();

    public ParametrizedMessageException() {
    }

    public ParametrizedMessageException(String message) {
        super(message);
    }

    protected void addParam(String key, String value) {
        this.paramsMap.put(key, value);
    }

}
