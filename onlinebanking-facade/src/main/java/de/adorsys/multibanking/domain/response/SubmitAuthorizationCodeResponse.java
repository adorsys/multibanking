package de.adorsys.multibanking.domain.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorizationCodeResponse extends AbstractResponse {

    private String status;
    private String transactionId;

}
