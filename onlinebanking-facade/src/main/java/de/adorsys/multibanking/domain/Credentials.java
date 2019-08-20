package de.adorsys.multibanking.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Credentials {
    private String customerId; //XS2A PSU-ID
    private String userId; //XS2A PSU-CORPORATE-ID
    private String pin;
    private String pin2;
}
