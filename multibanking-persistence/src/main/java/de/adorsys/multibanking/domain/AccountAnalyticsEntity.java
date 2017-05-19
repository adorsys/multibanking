package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 08.05.17.
 */
@Data
@Document
@Encrypted(exclude = {"_id", "accountId", "userId", "analyticsDate"})
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'accountId': 1}")
})
public class AccountAnalyticsEntity {

    @Id
    private String id;
    private String accountId;
    private String userId;

    private LocalDate analyticsDate = LocalDate.now();

    private BigDecimal incomeTotal = new BigDecimal(0);
    private BigDecimal incomeFixed = new BigDecimal(0);
    private BigDecimal incomeNext = new BigDecimal(0);

    private BigDecimal expensesTotal = new BigDecimal(0);
    private BigDecimal expensesFixed = new BigDecimal(0);
    private BigDecimal expensesNext = new BigDecimal(0);

    private BigDecimal balanceCalculated = new BigDecimal(0);

}
