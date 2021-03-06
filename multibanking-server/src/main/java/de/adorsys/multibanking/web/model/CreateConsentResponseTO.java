package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "ConsentCreated")
@Data
public class CreateConsentResponseTO {
    private ScaApproachTO scaApproach;
    private String consentId;
    private String authorisationId;
}
