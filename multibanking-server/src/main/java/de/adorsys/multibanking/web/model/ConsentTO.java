package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class ConsentTO {

    @NotNull
    private String psuAccountIban;

    @ApiModelProperty(value = "consent accounts for details")
    private List<AccountReferenceTO> accounts;
    @ApiModelProperty(value = "consent accounts for balances")
    private List<AccountReferenceTO> balances;
    @ApiModelProperty(value = "consent accounts for transactions")
    private List<AccountReferenceTO> transactions;

    @NotNull
    @ApiModelProperty(value = "recurring indicator")
    private boolean recurringIndicator;
    @Future
    @NotNull
    @ApiModelProperty(value = "consent valid date")
    private LocalDate validUntil;
    @NotNull
    @ApiModelProperty(value = "allowed access frequency per day")
    private int frequencyPerDay;

}
