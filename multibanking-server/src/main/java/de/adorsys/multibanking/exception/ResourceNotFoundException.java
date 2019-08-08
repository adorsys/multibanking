package de.adorsys.multibanking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(
    value = HttpStatus.NOT_FOUND,
    reason = "RESOURCE_NOT_FOUND"
)
public class ResourceNotFoundException extends ParametrizedMessageException {
    public ResourceNotFoundException(Class<?> resourceClazz, String businessKey) {
        super(MessageFormat.format("Resource [{0}] mit Key [{1}] nicht gefunden.",
            new Object[]{resourceClazz.getSimpleName(), businessKey}));
        this.addParam("resource", resourceClazz.getSimpleName());
        this.addParam("businessKey", businessKey);
    }
}
