package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(name = "Consent")
@Data
public class ConsentTO {

    @Schema(description = "Consent id", accessMode = READ_ONLY)
    private String id;

    @Schema(description = "Defines consent as temporary in case of OAUTH_PRESTEP appraoch", accessMode = READ_ONLY)
    private boolean temporary;

    @Schema(description = "Consent redirect id")
    private String redirectId;

    // @NotNull
    @Schema(description = "Consent psu id")
    private String psuId;

    @NotNull
    @Schema(description = "account iban", required = true)
    private String psuAccountIban;

    @Schema(description = "URI of the TPP, where the transaction flow shall be redirected to after a Redirect.\n" +
        "        \n" +
        "        Mandated for the Redirect SCA Approach, specifically \n" +
        "        when TPP-Redirect-Preferred equals \"true\".\n" +
        "        It is recommended to always use this header field.")
    private String tppRedirectUri;

    @Schema(description = "consent accounts for details")
    private List<AccountReferenceTO> accounts;
    @Schema(description = "consent accounts for balances")
    private List<AccountReferenceTO> balances;
    @Schema(description = "consent accounts for transactions")
    private List<AccountReferenceTO> transactions;

    @NotNull
    @Schema(description = "recurring indicator")
    private boolean recurringIndicator;
    @Future
    @NotNull
    @Schema(description = "consent valid date")
    private LocalDate validUntil;
    @NotNull
    @Schema(description = "allowed access frequency per day")
    private int frequencyPerDay;

}
