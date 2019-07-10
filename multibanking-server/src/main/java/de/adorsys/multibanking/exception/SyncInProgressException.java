package de.adorsys.multibanking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

/**
 * Created by alexg on 19.05.17.
 */
@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "SYNC_IN_PROGRESS"
)
public class SyncInProgressException extends ParametrizedMessageException {

    public SyncInProgressException(String account) {
        super(MessageFormat.format("Account [{0}] sync in progress.", new Object[]{account}));
        this.addParam("account", account);
    }
}
