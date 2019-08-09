package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAuthResponseTO {
    @ApiModelProperty(value = "This data element might be contained, if SCA is required and if the PSU has a choice " +
        "between different authentication methods. These methods shall be presented towards the PSU for selection by " +
        "the TPP.")
    private List<TanTransportTypeTO> scaMethods;

    private ScaStatusTO scaStatus;

    @ApiModelProperty(value = "It is contained in addition to the data element " +
        "'chosenScaMethod' if challenge data is needed for SCA.")
    private ChallengeDataTO challenge;

    @ApiModelProperty(value = "Text to be displayed to the PSU")
    private String psuMessage;
}
