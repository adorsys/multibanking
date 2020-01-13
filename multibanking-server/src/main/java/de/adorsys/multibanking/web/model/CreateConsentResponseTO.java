package de.adorsys.multibanking.web.model;

import lombok.Data;

@Data
public class CreateConsentResponseTO {
    private ScaApproachTO scaApproach;
    private String consentId;
    private String authorisationId;
}
