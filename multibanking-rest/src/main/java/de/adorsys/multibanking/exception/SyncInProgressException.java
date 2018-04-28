package de.adorsys.multibanking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

/**
 * Created by alexg on 19.05.17.
 * @author fpo 2018-04-07 09:39
 */
@ResponseStatus(
        code = HttpStatus.PROCESSING,
        value = HttpStatus.PROCESSING,
        reason = SyncInProgressException.MESSAGE_KEY
)
public class SyncInProgressException extends ParametrizedMessageException {
	private static final long serialVersionUID = -1252636120037862048L;
	public static final String MESSAGE_KEY = "synch.in.progress";
	public static final String RENDERED_MESSAGE_KEY = "Sync in progress for account with accessId [{0}] and accountId [{1}].";
	public static final String MESSAGE_DOC = MESSAGE_KEY + ": Sync in progress for account with given accessId and accountId.";
	public static final int SC_PROCESSING = 102;

    public SyncInProgressException(String accessId, String accountId) {
        super(String.format(RENDERED_MESSAGE_KEY, accessId, accountId));
        this.addParam("accessId", accessId);
        this.addParam("accountId", accountId);
    }
}
