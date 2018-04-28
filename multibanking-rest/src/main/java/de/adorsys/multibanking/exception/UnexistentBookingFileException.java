package de.adorsys.multibanking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.adorsys.multibanking.exception.base.ParametrizedMessageException;

@ResponseStatus(
        code = HttpStatus.BAD_REQUEST,
        value = HttpStatus.BAD_REQUEST,
        reason = UnexistentBookingFileException.MESSAGE_KEY
)
public class UnexistentBookingFileException extends ParametrizedMessageException {
	private static final long serialVersionUID = -1836646959951727323L;
	public static final String MESSAGE_KEY = "unknown.booking.file";
	public static final String RENDERED_MESSAGE_KEY = "Booking file at address [{0}] unexistent";
	public static final String MESSAGE_DOC = MESSAGE_KEY + ": Booking file for provided address unexistent. Review the list of existing booking periods.";
	public UnexistentBookingFileException(String bookingFileFqn) {
        super(String.format(RENDERED_MESSAGE_KEY, bookingFileFqn));
        this.addParam("bookingFileFqn", bookingFileFqn);
    }
}
