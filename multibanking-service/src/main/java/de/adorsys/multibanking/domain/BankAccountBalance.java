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

    private BigDecimal readyHbciBalance = new BigDecimal("0.00");
    private BigDecimal unreadyHbciBalance = new BigDecimal("0.00");
    private BigDecimal creditHbciBalance = new BigDecimal("0.00");
    private BigDecimal availableHbciBalance = new BigDecimal("0.00");
    private BigDecimal usedHbciBalance = new BigDecimal("0.00");

}
