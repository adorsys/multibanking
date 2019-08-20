package de.adorsys.multibanking.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialsTO {
    private String bankLogin;
    private String bankLogin2;
    private String pin;
    private String pin2;
}
