package de.adorsys.multibanking.web.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenRequestTO {

    @NotBlank
    private String authorisationCode;
}
