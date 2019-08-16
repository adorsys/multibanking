package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.domain.Bank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
@CompoundIndexes({
        @CompoundIndex(name = "bank_index", def = "{'bankCode': 1}")
})
public class BankMongoEntity extends Bank {

    @Id
    private String id;
    private String bankApiBankCode;

    @Indexed
    private List<String> searchIndex;

}
