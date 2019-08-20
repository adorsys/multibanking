package de.adorsys.multibanking.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Credentials {
    private String bankLogin;
    private String bankLogin2;
    private String pin;
    private String pin2;
}
