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
    private String customerId;
    private String userId;
    private String pin;
    private String pin2;
}
