package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.hateoas.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString(onlyExplicitlyIncluded = true)
@Relation(collectionRelation = "bookingList")
@ApiModel(description = "Single bank booking", value = "Booking")
public class BookingTO {

    @ApiModelProperty(value = "Booking ID")
    private String id;

    @ToString.Include
    @ApiModelProperty(value = "External ID of this booking")
    private String externalId;

    @ApiModelProperty(value = "Opposite bank account")
    private BankAccountTO otherAccount;

    @ApiModelProperty(value = "Date on which the transaction gets effective", example = "2018-02-28")
    private LocalDate valutaDate;

    @ApiModelProperty(value = "Booking date", example = "2018-02-28")
    private LocalDate bookingDate;

    @ApiModelProperty(value = "Target amount", example = "-19.93")
    private BigDecimal amount;

    @ApiModelProperty(value = "Currency", example = "EUR")
    private String currency;

    @ApiModelProperty(value = "Does this reverses a preexisting booking", example = "false")
    private boolean reversal;

    @ApiModelProperty(value = "Account balance after this booking", example = "1849.1")
    private BigDecimal balance;

    @ApiModelProperty(value = "Reference of the opposite party", example = "NONREF")
    private String customerRef;

    @ApiModelProperty(value = "Reference of the corresponding institution")
    private String instRef;

    @ApiModelProperty(value = "Original value", example = "-19.93")
    private BigDecimal origValue;

    @ApiModelProperty(value = "Charge value", example = "-19.93")
    private BigDecimal chargeValue;

    @ApiModelProperty(value = "Transaction information", example = "KARTENZAHLUNG")
    private String text;

    @ApiModelProperty(value = "Additional transaction information", example = "KARTENZAHLUNG")
    private String additional;

    @ApiModelProperty(value = "Primanota")
    private String primanota;

    @ApiModelProperty(value = "Usage of this transaction", example = "Svwz+2018-02-27t11.47.44 Karte3 2020-12 " +
        "Abwa+6850 Edeka//Nuernberg/De")
    private String usage;

    @ApiModelProperty
    private String addkey;

    @ApiModelProperty(value = "Is this a SEPA transaction", example = "true")
    private boolean sepa;

    @ApiModelProperty(value = "Is this a standing order transaction", example = "false")
    private boolean standingOrder;

    @ApiModelProperty(value = "Creditor ID", example = "5aab866d31a02a0001f72cd5")
    private String creditorId;

    @ApiModelProperty(value = "Reference for the SEPA mandate")
    private String mandateReference;

    @ApiModelProperty(value = "Origin of this booking", example = "MOCK")
    private BankApiTO bankApi;

    @ApiModelProperty(value = "Category of this booking")
    private BookingCategoryTO bookingCategory;

    @ApiModelProperty(value = "Transaction type as DTA Tx Key code", example = "4")
    private String transactionCode;
}
