package de.adorsys.multibanking.hbci.domain;

import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.TanTransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HBCIConsentEntity {

    private String id;
    private String authorisationId;
    private String psuAccountIban;
    private ScaStatus status;
    private List<TanTransportType> tanMethodList;
    private TanTransportType selectedMethod;
}
