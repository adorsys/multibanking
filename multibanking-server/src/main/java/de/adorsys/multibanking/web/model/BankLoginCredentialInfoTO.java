package de.adorsys.multibanking.web.model;

import lombok.Data;

@Data
public class BankLoginCredentialInfoTO {

    private String label;
    private String fieldName;
    private boolean masked;
    private boolean optional;

}
