package domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@ApiModel(description="Single bank booking", value="Booking" )
public class Booking {

    @ApiModelProperty(value = "External ID of this booking", required=true)
    private String externalId;
    
    @ApiModelProperty(value = "Opposite bank account")
    private BankAccount otherAccount;
    
    @ApiModelProperty(value = "Date on which the transaction gets effective", example="2012-12-01")
    private LocalDate valutaDate;
    
    @ApiModelProperty(value = "Booking date", example="2012-12-01")
    private LocalDate bookingDate;
    
    @ApiModelProperty(value = "Target amount", example="500")
    private BigDecimal amount;

    @ApiModelProperty(value = "Does this reverses a preexisting booking", example="false")
    private boolean isReversal;
    
    @ApiModelProperty(value = "Account balance after this booking", example="2300")
    private BigDecimal balance;
    
    @ApiModelProperty(value = "Reference of the opposite party", example="Aldi Sued")
    private String customerRef;
    
    @ApiModelProperty(value = "Reference of the corresponding institution")
    private String instRef;
    
    @ApiModelProperty(value = "Original value", example="500")
    private BigDecimal origValue;
    
    @ApiModelProperty(value = "Charge value", example="500")
    private BigDecimal chargeValue;
    
    @ApiModelProperty(value = "Transaction information", example="Banana")
    private String text;

    @ApiModelProperty(value = "Additional transaction information", example="Banana")
    private String additional;
    
    @ApiModelProperty(value = "Primanota")
    private String primanota;
    
    @ApiModelProperty(value = "Usage of this transaction")
    private String usage;
    
    @ApiModelProperty(value = "")
    private String addkey;
    
    @ApiModelProperty(value = "Is this a SEPA transaction", example="true")
    private boolean isSepa;

    @ApiModelProperty(value = "Is this a standing order transaction", example="false")
    private boolean isStandingOrder;

    @ApiModelProperty(value = "Creditor ID", example="De66zzz00001861569")
    private String creditorId;

    @ApiModelProperty(value = "Reference for the SEPA mandate")
    private String mandateReference;

    @ApiModelProperty(value = "Origin of this booking", example="HBCI")
    private BankApi bankApi;
    
    @ApiModelProperty(value = "Category of this booking")
    private BookingCategory bookingCategory;

    @ApiModelProperty(value = "Transaction type as DTA Tx Key code", example = "4")
    private String transactionCode;
}
