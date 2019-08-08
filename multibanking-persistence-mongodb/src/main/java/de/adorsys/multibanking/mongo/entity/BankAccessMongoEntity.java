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
@JsonIgnoreProperties(value = {"pin", "pin2", "hbciPassportState"}, allowSetters = true)
@Encrypted(exclude = {"_id", "userId", "bankCode", "psd2ConsentId"})
public class BankAccessMongoEntity extends BankAccess {

    @Id
    private String id;
    private String psd2ConsentId;
    @Indexed
    private String userId;
    private String pin;
    private String pin2;
    private boolean temporary;
    private boolean storePin;
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
