package de.adorsys.multibanking.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Created by alexg on 01.12.17.
 */
@Data
@Document
@CompoundIndexes({
        @CompoundIndex(name = "creditor_unique_index", def = "{'creditorId': 1}", unique = true)
})
public class AnonymizedBookingEntity {

    private String creditorId;
    private BigDecimal amount;
    private String purpose;
}
