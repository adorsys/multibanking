package domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class Booking {

    private String externalId;
    private BankAccount otherAccount;
    private Date valutaDate;
    private Date bookingDate;
    private BigDecimal amount;
    private boolean isReversal;
    private BigDecimal balance;
    private String customerRef;
    private String instRef;
    private BigDecimal origValue;
    private BigDecimal chargeValue;
    private String additional;
    private String text;
    private String primanota;
    private String usage;
    private String addkey;
    private boolean isSepa;
    private BankApi bankApi;

}
