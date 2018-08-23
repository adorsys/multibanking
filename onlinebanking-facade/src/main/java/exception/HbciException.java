package exception;

import lombok.Data;

/**
 * Created by alexg on 21.11.17.
 */
@Data
public class HbciException extends RuntimeException {

    public HbciException(String msg) {
        this.message = msg;
    }

    private String message;

}
