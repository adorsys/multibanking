package exception;

import lombok.Data;

/**
 * Created by alexg on 21.11.17.
 */
@Data
public class HbciException extends RuntimeException {

    private String message;

    public HbciException(String msg) {
        this.message = msg;
    }

}
