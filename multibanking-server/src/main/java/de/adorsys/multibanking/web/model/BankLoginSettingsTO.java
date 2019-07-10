package de.adorsys.multibanking.web.model;

import lombok.Data;

import java.util.List;

@Data
public class BankLoginSettingsTO {

    private String icon;
    private List<BankLoginCredentialInfoTO> credentials;
    private String auth_type;
    private String advice;
}
