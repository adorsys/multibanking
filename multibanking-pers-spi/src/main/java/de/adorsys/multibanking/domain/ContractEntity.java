package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BankAccess;
import domain.Contract;
import domain.Cycle;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Created by alexg on 07.02.17.
 */
@Data
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
    private String mainCategoryName;
    private String subCategory;
    private String subCategoryName;
    private String specification;
    private String specificationName;
    private String provider;

}
