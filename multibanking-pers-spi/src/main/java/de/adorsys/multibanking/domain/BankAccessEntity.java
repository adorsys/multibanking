package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankAccess;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@JsonIgnoreProperties(value = {"pin", "pin2", "hbciPassportState"}, allowSetters = true)
@Encrypted(exclude = {"_id", "userId"})
public class BankAccessEntity extends BankAccess {

    @Id
    private String id;
    @Indexed
    private String userId;
    private String pin;
    private String pin2;
    private boolean temporary;
    private boolean storePin;
    private boolean storeBookings;
    private boolean categorizeBookings;
    private boolean storeAnalytics;

    public BankAccessEntity id(String id) {
        this.id = id;
        return this;
    }
}
