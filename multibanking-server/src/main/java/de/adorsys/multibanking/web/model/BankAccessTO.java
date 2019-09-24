package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

@Data
@ApiModel(description = "BankAccess account information", value = "BankAccess")
@Relation(collectionRelation = "bankAccessList")
public class BankAccessTO {

    @ApiModelProperty(value = "Internal bank access id", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String id;
    @ApiModelProperty(value = "PSD2 consent id", required = true)
    private String consentId;

    @ApiModelProperty(value = "IBAN", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String iban;

    @ApiModelProperty(value = "Bank name", example = "Deutsche Bank", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String bankName;

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

}
