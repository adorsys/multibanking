package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
@Encrypted(exclude = {"_id", "redirectId"})
public class ConsentMongoEntity {

    @Id
    private String id;
    private String authorisationId;
    @Indexed
    private String redirectId;
    private boolean temporary;
    private BankApi bankApi;
    private String psuAccountIban;
    private Object bankApiConsentData;
}
