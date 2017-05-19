package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankAccess;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@JsonIgnoreProperties(value = {"pin", "passportState", "externalIdMap"}, allowSetters = true)
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

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        BankAccessEntity bankAccessEntity = new BankAccessEntity();
        bankAccessEntity.setPin("pin12345");
        bankAccessEntity.setUserId("userasdfasdf");

        try {
            System.out.println(mapper.writeValueAsString(bankAccessEntity));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String jsonString = "{ \"pin\":\"pin12345\",\"userId\":\"userasdfasdf\" }";
        try {
            bankAccessEntity = mapper.readValue(jsonString, BankAccessEntity.class);

            System.out.println(bankAccessEntity.getPin());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
