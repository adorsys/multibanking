package de.adorsys.multibanking.jpa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name="ml_anonymized_booking")
@NoArgsConstructor
@Data
public class MlAnonymizedBookingJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
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