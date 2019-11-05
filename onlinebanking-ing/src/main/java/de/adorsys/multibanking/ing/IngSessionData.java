package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.ScaStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
class IngSessionData {

    private ScaStatus status;
    private String accessToken;
    private LocalDateTime expirationTime;
    private String refreshToken;
}
