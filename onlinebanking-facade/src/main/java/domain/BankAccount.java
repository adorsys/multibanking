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
@ApiModel(description="The bank account object.", value="BankAccount" )
public class BankAccount {

    public enum SyncStatus {
        PENDING, SYNC, READY
    }

	@ApiModelProperty(value = "A bank account can have an id with each API provider")
    private Map<BankApi, String> externalIdMap = new HashMap<>();
	
    @ApiModelProperty(value = "The bank account balance.", example="2.000,00")
    private BankAccountBalance bankAccountBalance;
	
    @ApiModelProperty(value = "The name of the owner of this account", example="Max Musterman")
    private String owner;

    @ApiModelProperty(value = "The ISO2 Country of this bank account", example="DE")
    private String country;
	
    @ApiModelProperty(value = "The bank code", example="76070024")
    private String blz;

    @ApiModelProperty(value = "The bank name", example="Deutsche Bank")
    private String bankName;

    @ApiModelProperty(value = "The account number", example="430254900")
    private String accountNumber;
	
    @ApiModelProperty(value = "The type of this bank account", example="Current Account")
    private String type;
	
    @ApiModelProperty(value = "The currency of this bank account", example="EURO")
    private String currency;
	
    @ApiModelProperty(value = "The name of this bank account if any")
    private String name;
	
    @ApiModelProperty(value = "The banc identification code", example="DEUTNL2A")
    private String bic;
	
    @ApiModelProperty(value = "The international banc account number", example="NL99DEU7430254900", required=true)
    private String iban;

	@ApiModelProperty(value = "The synchronisation status", example="PENDING")
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
