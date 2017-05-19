package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankAccess;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@JsonIgnoreProperties({"getPassportState", "getPin"})
@Encrypted(exclude = {"_id", "userId"})
public class BankAccessEntity extends BankAccess {

    @Id
    private String id;
    @Indexed
    private String userId;
    private String pin;

    public BankAccessEntity id(String id) {
        this.id = id;
        return this;
    }
}
