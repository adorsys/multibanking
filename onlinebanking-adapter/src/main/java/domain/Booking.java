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

    public String getExternalId() {
        return externalId;
    }

    public Booking externalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public BankAccount getOtherAccount() {
        return otherAccount;
    }

    public Booking otherAccount(BankAccount otherAccount) {
        this.otherAccount = otherAccount;
        return this;
    }

    public Date getValutaDate() {
        return valutaDate;
    }

    public Booking valutaDate(Date valutaDate) {
        this.valutaDate = valutaDate;
        return this;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public Booking bookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Booking amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public boolean isReversal() {
        return isReversal;
    }

    public Booking reversal(boolean reversal) {
        isReversal = reversal;
        return this;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Booking balance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    public String getCustomerRef() {
        return customerRef;
    }

    public Booking customerRef(String customerRef) {
        this.customerRef = customerRef;
        return this;
    }

    public String getInstRef() {
        return instRef;
    }

    public Booking instRef(String instRef) {
        this.instRef = instRef;
        return this;
    }

    public BigDecimal getOrigValue() {
        return origValue;
    }

    public Booking origValue(BigDecimal origValue) {
        this.origValue = origValue;
        return this;
    }

    public BigDecimal getChargeValue() {
        return chargeValue;
    }

    public Booking chargeValue(BigDecimal chargeValue) {
        this.chargeValue = chargeValue;
        return this;
    }

    public String getAdditional() {
        return additional;
    }

    public Booking additional(String additional) {
        this.additional = additional;
        return this;
    }

    public String getText() {
        return text;
    }

    public Booking text(String text) {
        this.text = text;
        return this;
    }

    public String getPrimanota() {
        return primanota;
    }

    public Booking primanota(String primanota) {
        this.primanota = primanota;
        return this;
    }

    public String getUsage() {
        return usage;
    }

    public Booking usage(String usage) {
        this.usage = usage;
        return this;
    }

    public String getAddkey() {
        return addkey;
    }

    public Booking addkey(String addkey) {
        this.addkey = addkey;
        return this;
    }

    public boolean isSepa() {
        return isSepa;
    }

    public Booking sepa(boolean sepa) {
        isSepa = sepa;
        return this;
    }
}
