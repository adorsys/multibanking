package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class ConsentTO {

    @ApiModelProperty(value = "consent id", readOnly = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String consentId;
    @ApiModelProperty(value = "consent authorisation id", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String consentAuthorisationId;

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

    @ApiModelProperty(value = "consent authorisation status", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private ScaStatusTO scaStatus;

    @ApiModelProperty(value = "consent authorisation method list", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private List<TanTransportTypeTO> scaMethodList;

    @ApiModelProperty(value = "selected consent authorisation method", accessMode =
        ApiModelProperty.AccessMode.READ_WRITE)
    private String scaMethodId;

    @ApiModelProperty(value = "tan for selected consent authorisation method", accessMode =
        ApiModelProperty.AccessMode.READ_WRITE)
    private String tan;

    @ApiModelProperty(value = "challenge for consent authorisation", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private ChallengeTO challenge;

    // FIXME move to challenge response
    @Deprecated()
    @ApiModelProperty(value = "authorisation redirect url", readOnly = true, accessMode =
        ApiModelProperty.AccessMode.READ_ONLY)
    private String redirectUrl;

}
