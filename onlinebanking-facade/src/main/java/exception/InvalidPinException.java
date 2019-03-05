package exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Created by alexg on 29.06.17.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class InvalidPinException extends RuntimeException {

    private String errorCode;
}
