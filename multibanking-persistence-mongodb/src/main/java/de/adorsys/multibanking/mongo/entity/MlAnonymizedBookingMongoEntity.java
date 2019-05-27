package de.adorsys.multibanking.mongo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document
public class MlAnonymizedBookingMongoEntity {

    @Id
    private String id;
    @Indexed
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