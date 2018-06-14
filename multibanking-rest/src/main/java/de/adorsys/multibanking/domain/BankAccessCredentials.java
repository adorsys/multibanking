package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.AbstractId;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Data
public class BankAccessCredentials extends AbstractId {
    private final static Logger LOGGER = LoggerFactory.getLogger(BankAccessCredentials.class);

    private String accessId;
    private String userId;
    private String pin;
    private String pin2;
    private String hbciPassportState;

    private Boolean pinValid = true;
    private Date lastValidationDate;
}
