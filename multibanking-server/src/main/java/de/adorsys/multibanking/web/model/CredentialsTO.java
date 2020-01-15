package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Credentials")
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
