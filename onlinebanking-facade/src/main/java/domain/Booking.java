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
@ApiModel(description="The balance of this bank account.", value="BankAccountBalance" )
public class Booking {

    @ApiModelProperty(value = "The external id of this booking", required=true)
    private String externalId;
    
    @ApiModelProperty(value = "The opposite bank account")
    private BankAccount otherAccount;
    
    @ApiModelProperty(value = "The value date", example="2012-12-01")
    private LocalDate valutaDate;
    
    @ApiModelProperty(value = "The booking date", example="2012-12-01")
    private LocalDate bookingDate;
    
    @ApiModelProperty(value = "The target aount", example="500")
    private BigDecimal amount;

    @ApiModelProperty(value = "Does this reverses a preexisting booking", example="false")
    private boolean isReversal;
    
    @ApiModelProperty(value = "The account balance after this booking", example="2300")
    private BigDecimal balance;
    
    @ApiModelProperty(value = "The reference of the opposite party", example="Aldi Sued")
    private String customerRef;
    
    @ApiModelProperty(value = "The reference of the corresponding institution")
    private String instRef;
    
    @ApiModelProperty(value = "The original value", example="500")
    private BigDecimal origValue;
    
    @ApiModelProperty(value = "The charge value", example="500")
    private BigDecimal chargeValue;
    
    @ApiModelProperty(value = "Transaction information", example="Banana")
    private String text;

    @ApiModelProperty(value = "Additional transation info", example="Banana")
    private String additional;
    
    @ApiModelProperty(value = "The primanota")
    private String primanota;
    
    @ApiModelProperty(value = "The usage of this transaction")
    private String usage;
    
    @ApiModelProperty(value = "Todo description needed")
    private String addkey;
    
    @ApiModelProperty(value = "Is this a sepa transaction", example="true")
    private boolean isSepa;
    
    @ApiModelProperty(value = "The origine of this booking", example="HBCI")
    private BankApi bankApi;
    
    @ApiModelProperty(value = "Categories of this booking")
    private BookingCategory bookingCategory;

    @ApiModelProperty(value = "Transaction type as DTA Tx Key code.", example = "4")
    private String transactionCode;
}
