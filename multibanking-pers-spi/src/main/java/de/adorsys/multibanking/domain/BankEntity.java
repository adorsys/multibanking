package de.adorsys.multibanking.domain;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import domain.Bank;

/**
 * Created by alexg on 08.05.17.
 */
@Data
@Document
@CompoundIndexes({
        @CompoundIndex(name = "bank_index", def = "{'bankCode': 1}")
})
public class BankEntity extends Bank {

    @Id
    private String id;
    private String blzHbci;
    @Indexed
    private List<String> searchIndex;

}
