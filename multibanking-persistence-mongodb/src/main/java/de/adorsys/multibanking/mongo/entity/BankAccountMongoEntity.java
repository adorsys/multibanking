package de.adorsys.multibanking.mongo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
@Encrypted(exclude = {"_id", "bankAccessId", "userId", "syncStatus", "rulesVersion"})
@JsonIgnoreProperties(value = {"externalIdMap"}, allowSetters = true)
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'bankAccessId': 1}")
})
public class BankAccountMongoEntity extends BankAccount {

    @Id
    private String id;
    private String bankAccessId;
    private String userId;
    private String psd2ConsentId;
    private String psd2ConsentAuthorisationId;

}
