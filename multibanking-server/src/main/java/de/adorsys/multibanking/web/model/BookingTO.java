package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.hateoas.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Relation(collectionRelation = "bookingList")
@Schema(name = "Booking", description = "Single bank booking")
public class BookingTO {

    @Schema(description = "Booking ID")
    private String id;

    @ToString.Include
    @Schema(description = "External ID of this booking")
    private String externalId;

    @Schema(description = "Opposite bank account")
    private BankAccountTO otherAccount;

    @Schema(description = "Date on which the transaction gets effective", example = "2018-02-28")
    private LocalDate valutaDate;

    @Schema(description = "Booking date", example = "2018-02-28")
    private LocalDate bookingDate;

    @Schema(description = "Target amount", example = "-19.93")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "EUR")
    private String currency;

    @Schema(description = "Does this reverses a preexisting booking", example = "false")
    private boolean reversal;

    @Schema(description = "Account balance after this booking", example = "1849.1")
    private BigDecimal balance;

    @Schema(description = "Reference of the opposite party", example = "NONREF")
    private String customerRef;

    @Schema(description = "Reference of the corresponding institution")
    private String instRef;

    @Schema(description = "Original value", example = "-19.93")
    private BigDecimal origValue;

    @Schema(description = "Charge value", example = "-19.93")
    private BigDecimal chargeValue;

    @Schema(description = "Transaction information", example = "KARTENZAHLUNG")
    private String text;

    @Schema(description = "Additional transaction information", example = "KARTENZAHLUNG")
    private String additional;

    @Schema(description = "Primanota")
    private String primanota;

    @Schema(description = "Usage of this transaction", example = "Svwz+2018-02-27t11.47.44 Karte3 2020-12 " +
        "Abwa+6850 Edeka//Nuernberg/De")
    private String usage;

    private String addkey;

    @Schema(description = "Is this a SEPA transaction", example = "true")
    private boolean sepa;

    @Schema(description = "Is this a standing order transaction", example = "false")
    private boolean standingOrder;

    @Schema(description = "Creditor ID", example = "5aab866d31a02a0001f72cd5")
    private String creditorId;

    @Schema(description = "Reference for the SEPA mandate")
    private String mandateReference;

    @Schema(description = "Origin of this booking", example = "MOCK")
    private BankApiTO bankApi;

    @Schema(description = "Category of this booking")
    private BookingCategoryTO bookingCategory;

    @Schema(description = "Transaction type as DTA Tx Key code", example = "4")
    private String transactionCode;

    @Schema(description = "Proprietary bank transaction code", example = "NTRF+117+009000")
    private String proprietaryBankTransactionCode;
}
