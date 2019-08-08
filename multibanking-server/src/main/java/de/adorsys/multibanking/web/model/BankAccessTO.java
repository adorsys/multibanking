package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "BankAccess account information", value = "BankAccess")
@Relation(collectionRelation = "bankAccessList")
public class BankAccessTO {

    @ApiModelProperty(value = "Internal bank access id", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String id;
    @NotBlank
    @ApiModelProperty(value = "IBAN", required = true, example = "DE51250400903312345678")
    private String iban;
    @NotBlank
    @ApiModelProperty(value = "Bank login name", required = true, example = "l.name")
    private String bankLogin;
    @ApiModelProperty(value = "2nd bank login name", example = "1234567890")
    private String bankLogin2;
    @ApiModelProperty(value = "Bank access password")
    private String pin;
    @ApiModelProperty(value = "Bank access second password")
    private String pin2;

    @ApiModelProperty(value = "PSD2 consent id")
    private String psd2ConsentId;

    @ApiModelProperty(value = "Store pin")
    private boolean storePin;
    @ApiModelProperty(value = "Store bookings")
    private boolean storeBookings;
    @ApiModelProperty(value = "Categorize bookings")
    private boolean categorizeBookings;
    @ApiModelProperty(value = "Store analytics")
    private boolean storeAnalytics;
    @ApiModelProperty(value = "Store anonymized bookings")
    private boolean storeAnonymizedBookings;
    @ApiModelProperty(value = "Provide anonymized bookings for machine learning")
    private boolean provideDataForMachineLearning;

    @ApiModelProperty(value = "Bank name", example = "Deutsche Bank", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String bankName;

}
