package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Relation(collectionRelation = "bankAccountList")
@ApiModel(description = "The bank account object", value = "BankAccount")
public class BankAccountTO {

    @NotNull
    @ApiModelProperty(value = "Account ID")
    private String id;
    @NotNull
    @ApiModelProperty(value = "Bank Access Id")
    private String bankAccessId;
    @ApiModelProperty(value = "Bank account balances")
    private BalancesReportTO balances;
    @ApiModelProperty(value = "Name of the account owner", example = "EDEKA")
    private String owner;
    @ApiModelProperty(value = "ISO-2 country of this bank account", example = "DE")
    private String country;
    @ApiModelProperty(value = "Bank code", example = "29999999")
    private String blz;
    @ApiModelProperty(value = "Bank name", example = "Mock Bank")
    private String bankName;
    @ApiModelProperty(value = "Account number", example = "1234567890")
    private String accountNumber;
    @ApiModelProperty(value = "Type of this bank account", example = "GIRO")
    private BankAccountTypeTO type;
    @ApiModelProperty(value = "Currency of this bank account", example = "EURO")
    private String currency;
    @ApiModelProperty(value = "Name of this bank account")
    private String name;
    @ApiModelProperty(value = "Bank identification code", example = "EDEKDEHHXXX")
    private String bic;
    @ApiModelProperty(value = "International bank account number", example = "DE50200907003443582071", required = true)
    private String iban;
    @ApiModelProperty(value = "Synchronisation status", example = "PENDING")
    private SyncStatusTO syncStatus;
    @ApiModelProperty(value = "Last Synchronisation date", example = "2017-12-01")
    private LocalDateTime lastSync;

    public enum SyncStatusTO {
        PENDING, SYNC, READY
    }

    public enum BankAccountTypeTO {
        GIRO, SAVINGS, FIXEDTERMDEPOSIT, DEPOT, LOAN, CREDITCARD, BUIILDINGSAVING, INSURANCE, UNKNOWN;
    }
}
