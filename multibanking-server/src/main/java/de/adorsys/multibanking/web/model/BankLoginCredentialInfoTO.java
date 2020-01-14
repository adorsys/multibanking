package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "Bank login credentials info")
@Data
public class BankLoginCredentialInfoTO {

    private String label;
    private String fieldName;
    private boolean masked;
    private boolean optional;

}
