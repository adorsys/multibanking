package exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by alexg on 21.11.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HbciException extends RuntimeException {

    private String message;

    public HbciException(String msg) {
        this.message = msg;
    }

}
