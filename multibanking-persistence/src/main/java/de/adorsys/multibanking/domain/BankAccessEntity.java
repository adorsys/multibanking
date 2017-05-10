package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankAccess;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@JsonIgnoreProperties({"getPassportState"})
@Encrypted(fields = {"bankName", "bankLogin", "bankCode", "passportState"})
public class BankAccessEntity extends BankAccess {

    @Id
    private String id;
    @Indexed
    private String userId;
    @Transient
    private String pin;

    public BankAccessEntity id(String id) {
        this.id = id;
        return this;
    }

    public BankAccessEntity userId(String userId) {
        this.userId = userId;
        return this;
    }

    public BankAccessEntity pin(String pin) {
        this.pin = pin;
        return this;
    }
}
