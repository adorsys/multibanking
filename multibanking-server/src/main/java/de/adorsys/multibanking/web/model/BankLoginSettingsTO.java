package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(name = "BankLoginInfo")
@Data
public class BankLoginSettingsTO {

    private String icon;
    private List<BankLoginCredentialInfoTO> credentials;
    private String auth_type;
    private String advice;
}
