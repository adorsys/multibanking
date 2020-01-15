package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Data
@Schema(name = "BankAccess", description = "Bank access information")
@Relation(collectionRelation = "bankAccessList")
public class BankAccessTO {

    @Schema(description = "Internal bank access id", accessMode = READ_ONLY)
    private String id;
    @Schema(description = "PSD2 consent id", required = true)
    private String consentId;

    @Schema(description = "IBAN", accessMode = READ_ONLY)
    private String iban;

    @Schema(description = "Bank name", example = "Deutsche Bank", accessMode = READ_ONLY)
    private String bankName;

    @Schema(description = "Store bookings")
    private boolean storeBookings;
    @Schema(description = "Categorize bookings")
    private boolean categorizeBookings;
    @Schema(description = "Store analytics")
    private boolean storeAnalytics;
    @Schema(description = "Store anonymized bookings")
    private boolean storeAnonymizedBookings;
    @Schema(description = "Provide anonymized bookings for machine learning")
    private boolean provideDataForMachineLearning;

}
