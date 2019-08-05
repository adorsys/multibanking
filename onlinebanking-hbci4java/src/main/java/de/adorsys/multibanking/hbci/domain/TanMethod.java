package de.adorsys.multibanking.hbci.domain;

import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisation;
import lombok.Data;

@Data
public class TanMethod implements StrongCustomerAuthorisation {
    private String id;
    // FIXME add all needed information for the tan method of HBCI
}
