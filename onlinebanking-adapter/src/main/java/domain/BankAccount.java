package domain;

import lombok.Data;
import org.kapott.hbci.structures.Konto;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccount {

    private BankAccountBalance bankAccountBalance;
    private String countryHbciAccount;
    private String blzHbciAccount;
    private String numberHbciAccount;
    private String typeHbciAccount;
    private String currencyHbciAccount;
    private String nameHbciAccount;
    private String bicHbciAccount;
    private String ibanHbciAccount;

    public BankAccount() {
    }

    public BankAccount(Konto konto) {
        this.numberHbciAccount = konto.number;
        this.bicHbciAccount = konto.bic;
        this.blzHbciAccount = konto.blz;
        this.countryHbciAccount = konto.country;
        this.currencyHbciAccount = konto.curr;
        this.ibanHbciAccount = konto.iban;
        this.nameHbciAccount = (konto.name + " " + (konto.name2 != null ? konto.name2 : "")).trim();
        this.typeHbciAccount = konto.type;
    }

    public BankAccountBalance getBankAccountBalance() {
        return bankAccountBalance;
    }

    public BankAccount bankAccountBalance(BankAccountBalance bankAccountBalance) {
        this.bankAccountBalance = bankAccountBalance;
        return this;
    }

    public String getCountryHbciAccount() {
        return countryHbciAccount;
    }

    public BankAccount countryHbciAccount(String countryHbciAccount) {
        this.countryHbciAccount = countryHbciAccount;
        return this;
    }

    public String getBlzHbciAccount() {
        return blzHbciAccount;
    }

    public BankAccount blzHbciAccount(String blzHbciAccount) {
        this.blzHbciAccount = blzHbciAccount;
        return this;
    }

    public String getNumberHbciAccount() {
        return numberHbciAccount;
    }

    public BankAccount numberHbciAccount(String numberHbciAccount) {
        this.numberHbciAccount = numberHbciAccount;
        return this;
    }

    public String getTypeHbciAccount() {
        return typeHbciAccount;
    }

    public BankAccount typeHbciAccount(String typeHbciAccount) {
        this.typeHbciAccount = typeHbciAccount;
        return this;
    }

    public String getCurrencyHbciAccount() {
        return currencyHbciAccount;
    }

    public BankAccount currencyHbciAccount(String currencyHbciAccount) {
        this.currencyHbciAccount = currencyHbciAccount;
        return this;
    }

    public String getNameHbciAccount() {
        return nameHbciAccount;
    }

    public BankAccount nameHbciAccount(String nameHbciAccount) {
        this.nameHbciAccount = nameHbciAccount;
        return this;
    }

    public String getBicHbciAccount() {
        return bicHbciAccount;
    }

    public BankAccount bicHbciAccount(String bicHbciAccount) {
        this.bicHbciAccount = bicHbciAccount;
        return this;
    }

    public String getIbanHbciAccount() {
        return ibanHbciAccount;
    }

    public BankAccount ibanHbciAccount(String ibanHbciAccount) {
        this.ibanHbciAccount = ibanHbciAccount;
        return this;
    }
}
