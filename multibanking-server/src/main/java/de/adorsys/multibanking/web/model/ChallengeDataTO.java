package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "Challenge", description = "It is contained in addition to the data element 'chosenScaMethod' if challenge data is " +
    "needed for SCA")
public class ChallengeDataTO {

    @Schema(description = "PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. " +
        "[RFC4648].\n" +
        "This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method."
    )
    private String image;

    @Schema(description = "String challenge data")
    private List<String> data;

    @Schema(description = "A link where the ASPSP will provides the challenge image")
    private String imageLink;

    @Schema(description = "The maximal length for the OTP to be typed in by the PSU")
    private int otpMaxLength;

    @Schema(description = "The format type of the OTP to be typed in. The admitted values are \"characters\" or " +
        "\"integer\"")
    private String otpFormat;

    @Schema(description = "Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen " +
        "SCA method")
    private String additionalInformation;
}
