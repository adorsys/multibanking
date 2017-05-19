package domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccount {

    public enum SyncStatus {
        PENDING, SYNC, READY
    }

    private Map<BankApi, String> externalIdMap;
    private BankAccountBalance bankAccountBalance;
    private String owner;
    private String countryHbciAccount;
    private String blzHbciAccount;
    private String numberHbciAccount;
    private String typeHbciAccount;
    private String currencyHbciAccount;
    private String nameHbciAccount;
    private String bicHbciAccount;
    private String ibanHbciAccount;
    private SyncStatus syncStatus = SyncStatus.PENDING;

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

    public BankAccount externalId(BankApi bankApi, String externalId) {
        if (externalIdMap == null) {
            externalIdMap = new HashMap<>();
            externalIdMap.put(bankApi, externalId);
        }
        return this;
    }

    public BankAccount owner(String owner) {
        this.owner = owner;
        return this;
    }


}
