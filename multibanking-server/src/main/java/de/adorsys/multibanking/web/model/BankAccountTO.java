package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Relation(collectionRelation = "bankAccountList")
@Schema(name = "Bank account", description = "The bank account object")
public class BankAccountTO {

    @NotNull
    @Schema(description = "Account ID")
    private String id;
    @Schema(description = "User ID")
    private String userId;
    @NotNull
    @Schema(description = "Bank Access Id")
    private String bankAccessId;
    @Schema(description = "Bank account balances")
    private BalancesReportTO balances;
    @Schema(description = "Name of the account owner", example = "EDEKA")
    private String owner;
    @Schema(description = "ISO-2 country of this bank account", example = "DE")
    private String country;
    @Schema(description = "Bank code", example = "29999999")
    private String blz;
    @Schema(description = "Bank name", example = "Mock Bank")
    private String bankName;
    @Schema(description = "Account number", example = "1234567890")
    private String accountNumber;
    @Schema(description = "Type of this bank account", example = "GIRO")
    private BankAccountTypeTO type;
    @Schema(description = "Currency of this bank account", example = "EURO")
    private String currency;
    @Schema(description = "Name of this bank account")
    private String name;
    @Schema(description = "Bank identification code", example = "EDEKDEHHXXX")
    private String bic;
    @Schema(description = "International bank account number", example = "DE50200907003443582071", required = true)
    private String iban;
    @Schema(description = "Synchronisation status", example = "PENDING")
    private SyncStatusTO syncStatus;
    @Schema(description = "Last Synchronisation date", example = "2017-12-01")
    private LocalDateTime lastSync;

    public enum SyncStatusTO {
        PENDING, SYNC, READY
    }

    public enum BankAccountTypeTO {
        GIRO, SAVINGS, FIXEDTERMDEPOSIT, DEPOT, LOAN, CREDITCARD, BUIILDINGSAVING, INSURANCE, UNKNOWN;
    }
}
