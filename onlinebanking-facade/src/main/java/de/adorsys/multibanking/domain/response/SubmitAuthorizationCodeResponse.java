package de.adorsys.multibanking.domain.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorizationCodeResponse extends AbstractResponse {

    private String status;
    private String transactionId;

}
