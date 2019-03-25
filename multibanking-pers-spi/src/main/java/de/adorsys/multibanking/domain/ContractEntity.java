package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document
@Encrypted(exclude = {"_id", "accountId", "userId", "analyticsDate"})
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'accountId': 1}")
})
public class ContractEntity extends Contract {

    @Id
    private String id;
    private String userId;
    private String accountId;
    private BigDecimal amount;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String provider;

}
