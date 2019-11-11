package de.adorsys.multibanking.bg;

import lombok.Data;

@Data
class BgSessionData {
    private String authorisationId;
    private String accessToken;
    private String refreshToken;
}
