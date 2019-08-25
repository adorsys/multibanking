package de.adorsys.multibanking.mongo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
@JsonIgnoreProperties(value = {"hbciPassportState"}, allowSetters = true)
@Encrypted(exclude = {"_id", "userId", "bankCode", "consentId"})
public class BankAccessMongoEntity extends BankAccess {

    @Id
    private String id;
    @Indexed
    private String userId;
    private boolean temporary;
    private boolean storeBookings;
    private boolean categorizeBookings;
    private boolean storeAnalytics;
    private boolean storeAnonymizedBookings;
    private boolean provideDataForMachineLearning;

    public BankAccessMongoEntity id(String id) {
        this.id = id;
        return this;
    }
}
