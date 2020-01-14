package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(name = "Consent authorisation response")
@Data
public class UpdateAuthResponseTO {

    @Schema(description = "This data element might be contained, if SCA is required and if the PSU has a choice " +
        "between different authentication methods. These methods shall be presented towards the PSU for selection by " +
        "the TPP.")
    private List<TanTransportTypeTO> scaMethods;

    private ScaStatusTO scaStatus;

    @Schema(description = "It is contained in addition to the data element " +
        "'chosenScaMethod' if challenge data is needed for SCA.")
    private ChallengeDataTO challenge;

    @Schema(description = "Text to be displayed to the PSU")
    private String psuMessage;

    @Schema(description = "Type of sca approach to identify challenge handling")
    private ScaApproachTO scaApproach;
}
