package de.adorsys.multibanking.web.model;

import lombok.Data;

@Data
public class ConsentTO {

    private String consentId;
    private String authUrl;
    private ScaStatusTO scaStatus;

    public enum ScaStatusTO {
        STARTED, FINALISED, FAILED
    }

}
