package de.adorsys.multibanking.domain.response;

import lombok.Data;

@Data
public class SubmitAuthorizationCodeResponse {

    private String status;
    private String transactionId;

}
