package de.adorsys.multibanking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by alexg on 08.02.17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountBalance {

    private BigDecimal readyHbciBalance;
    private BigDecimal unreadyHbciBalance;
    private BigDecimal creditHbciBalance;
    private BigDecimal availableHbciBalance;
    private BigDecimal usedHbciBalance;

}
