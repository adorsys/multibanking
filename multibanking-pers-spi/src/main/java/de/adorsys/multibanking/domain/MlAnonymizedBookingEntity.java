package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MlAnonymizedBookingEntity {

    private String id;
    private String userId;
    private String transactionPurpose;
    private String amountBin;
    private String amountRemainder;
    private String creditorId;
    private String referenceName;
    private String executionDate;
    private String mainCategory;
    private String subCategory;
    private String specification;

}