package de.adorsys.multibanking.web.model;

import lombok.Data;

@Data
public class ConsentTO {

    private String authorisationUrl;
    private String redirectUrl;
    private ScaStatusTO scaStatus;

    public enum ScaStatusTO {
        RECEIVED,
        REJECTED,
        VALID,
        REVOKED_BY_PSU,
        EXPIRED,
        TERMINATED_BY_TPP,
        TERMINATED_BY_ASPSP,
        PARTIALLY_AUTHORISED;
    }

}
