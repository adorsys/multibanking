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
    @ApiModelProperty("URI of the TPP, where the transaction flow shall be redirected to after a Redirect.\n" +
        "        \n" +
        "        Mandated for the Redirect SCA Approach, specifically \n" +
        "        when TPP-Redirect-Preferred equals \"true\".\n" +
        "        It is recommended to always use this header field.")
    private String tppRedirectUri;

    @ApiModelProperty("consent accounts for details")
    private List<AccountReferenceTO> accounts;
    @ApiModelProperty("consent accounts for balances")
    private List<AccountReferenceTO> balances;
    @ApiModelProperty("consent accounts for transactions")
    private List<AccountReferenceTO> transactions;

    @NotNull
    @ApiModelProperty("recurring indicator")
    private boolean recurringIndicator;
    @Future
    @NotNull
    @ApiModelProperty("consent valid date")
    private LocalDate validUntil;
    @NotNull
    @ApiModelProperty("allowed access frequency per day")
    private int frequencyPerDay;

}
