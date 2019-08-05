package de.adorsys.multibanking.domain.response;

import de.adorsys.multibanking.domain.TanTransportType;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
abstract class AbstractResponse {

    private boolean authorisationRequired;
    private List<TanTransportType> tanTransportTypes;
    private List<String> messages;
}
