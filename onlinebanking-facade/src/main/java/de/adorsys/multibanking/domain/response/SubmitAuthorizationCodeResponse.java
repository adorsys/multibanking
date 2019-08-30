package de.adorsys.multibanking.domain.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorizationCodeResponse<T extends AbstractResponse> {

    private final T jobResponse;
    private String status;
    private String transactionId;

}
