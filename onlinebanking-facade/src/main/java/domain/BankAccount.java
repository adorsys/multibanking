package domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@ApiModel(description="The bank account object", value="BankAccount" )
public class BankAccount {

    public enum SyncStatus {
        PENDING, SYNC, READY
    }

    @ApiModelProperty(hidden = true)
    private Map<BankApi, String> externalIdMap = new HashMap<>();
	
    @ApiModelProperty(value = "Bank account balance", example="2.000,00")
    private BankAccountBalance bankAccountBalance;
	
    @ApiModelProperty(value = "Name of the account owner", example="Max Mustermann")
    private String owner;

    @ApiModelProperty(value = "ISO-2 country of this bank account", example="DE")
    private String country;
	
    @ApiModelProperty(value = "Bank code", example="76070024")
    private String blz;

    @ApiModelProperty(value = "Bank name", example="Deutsche Bank")
    private String bankName;

    @ApiModelProperty(value = "Account number", example="430254900")
    private String accountNumber;
	
    @ApiModelProperty(value = "Type of this bank account", example="Current Account")
    private String type;
	
    @ApiModelProperty(value = "Currency of this bank account", example="EURO")
    private String currency;
	
    @ApiModelProperty(value = "Name of this bank account")
    private String name;
	
    @ApiModelProperty(value = "Bank identification code", example="DEUTNL2A")
    private String bic;
	
    @ApiModelProperty(value = "International bank account number", example="DE41124500000009254912", required=true)
    private String iban;

	@ApiModelProperty(value = "Synchronisation status", example="PENDING")
	private SyncStatus syncStatus;

    public BankAccount bankAccountBalance(BankAccountBalance bankAccountBalance) {
        this.bankAccountBalance = bankAccountBalance;
        return this;
    }

    public BankAccount countryHbciAccount(String countryHbciAccount) {
        this.country = countryHbciAccount;
        return this;
    }

    public BankAccount blzHbciAccount(String blzHbciAccount) {
        this.blz = blzHbciAccount;
        return this;
    }

    public BankAccount numberHbciAccount(String numberHbciAccount) {
        this.accountNumber = numberHbciAccount;
        return this;
    }

    public BankAccount typeHbciAccount(String typeHbciAccount) {
        this.type = typeHbciAccount;
        return this;
    }

    public BankAccount currencyHbciAccount(String currencyHbciAccount) {
        this.currency = currencyHbciAccount;
        return this;
    }

    public BankAccount nameHbciAccount(String nameHbciAccount) {
        this.name = nameHbciAccount;
        return this;
    }

    public BankAccount bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public BankAccount bicHbciAccount(String bicHbciAccount) {
        this.bic = bicHbciAccount;
        return this;
    }

    public BankAccount ibanHbciAccount(String ibanHbciAccount) {
        this.iban = ibanHbciAccount;
        return this;
    }

    public BankAccount externalId(BankApi bankApi, String externalId) {
        if (externalIdMap == null) {
            externalIdMap = new HashMap<>();
        }
        externalIdMap.put(bankApi, externalId);
        return this;
    }

    public BankAccount owner(String owner) {
        this.owner = owner;
        return this;
    }


}
