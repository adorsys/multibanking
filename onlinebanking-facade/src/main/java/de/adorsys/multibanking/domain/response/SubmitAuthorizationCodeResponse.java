package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.ScaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorizationCodeResponse<T extends AbstractResponse> {

    private final T jobResponse;
    private List<String> warnings;
    private String transactionId;
    private ScaStatus scaStatus;

}
