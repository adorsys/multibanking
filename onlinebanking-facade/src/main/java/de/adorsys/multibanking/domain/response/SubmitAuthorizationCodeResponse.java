package de.adorsys.multibanking.domain.response;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorizationCodeResponse<T extends AbstractResponse> extends AbstractResponse {

    private String status;
    private String transactionId;
    private T jobResponse;

}
