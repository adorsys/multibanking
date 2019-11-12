package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.ScaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class TransactionAuthorisationResponse<T extends AbstractResponse> {

    private final T jobResponse;
    private ScaStatus scaStatus;

}
