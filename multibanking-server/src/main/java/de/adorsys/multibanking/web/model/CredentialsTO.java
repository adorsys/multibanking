package de.adorsys.multibanking.web.model;

import lombok.*;

@ToString(onlyExplicitlyIncluded = true)
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
