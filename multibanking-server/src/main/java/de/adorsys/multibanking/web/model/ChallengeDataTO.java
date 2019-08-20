package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "It is contained in addition to the data element 'chosenScaMethod' if challenge data is " +
    "needed for SCA")
public class ChallengeDataTO {
    @ApiModelProperty(value = "PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. " +
        "[RFC4648].\n" +
        "This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method."
    )
    private String image;

    @ApiModelProperty(value = "String challenge data")
    private String data;

    @ApiModelProperty(value = "A link where the ASPSP will provides the challenge image")
    private String imageLink;

    @ApiModelProperty(value = "The maximal length for the OTP to be typed in by the PSU")
    private int otpMaxLength;

    @ApiModelProperty(value = "The format type of the OTP to be typed in. The admitted values are \"characters\" or " +
        "\"integer\"")
    private String otpFormat;

    @ApiModelProperty(value = "Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen " +
        "SCA method")
    private String additionalInformation;
}
