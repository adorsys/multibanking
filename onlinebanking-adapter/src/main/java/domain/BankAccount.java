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

    public static BankAccount fromKonto(Konto konto) {
        BankAccount BankAccount = new BankAccount();
        BankAccount.numberHbciAccount(konto.number);
        BankAccount.bicHbciAccount(konto.bic);
        BankAccount.blzHbciAccount(konto.blz);
        BankAccount.countryHbciAccount(konto.country);
        BankAccount.currencyHbciAccount(konto.curr);
        BankAccount.ibanHbciAccount(konto.iban);
        BankAccount.nameHbciAccount((konto.name + " " + (konto.name2 != null ? konto.name2 : "")).trim());
        BankAccount.typeHbciAccount(konto.type);
        return BankAccount;
    }

    public BankAccount bankAccountBalance(BankAccountBalance bankAccountBalance) {
        this.bankAccountBalance = bankAccountBalance;
        return this;
    }

    public BankAccount countryHbciAccount(String countryHbciAccount) {
        this.countryHbciAccount = countryHbciAccount;
        return this;
    }

    public BankAccount blzHbciAccount(String blzHbciAccount) {
        this.blzHbciAccount = blzHbciAccount;
        return this;
    }

    public BankAccount numberHbciAccount(String numberHbciAccount) {
        this.numberHbciAccount = numberHbciAccount;
        return this;
    }

    public BankAccount typeHbciAccount(String typeHbciAccount) {
        this.typeHbciAccount = typeHbciAccount;
        return this;
    }

    public BankAccount currencyHbciAccount(String currencyHbciAccount) {
        this.currencyHbciAccount = currencyHbciAccount;
        return this;
    }

    public BankAccount nameHbciAccount(String nameHbciAccount) {
        this.nameHbciAccount = nameHbciAccount;
        return this;
    }

    public BankAccount bicHbciAccount(String bicHbciAccount) {
        this.bicHbciAccount = bicHbciAccount;
        return this;
    }

    public BankAccount ibanHbciAccount(String ibanHbciAccount) {
        this.ibanHbciAccount = ibanHbciAccount;
        return this;
    }
}
