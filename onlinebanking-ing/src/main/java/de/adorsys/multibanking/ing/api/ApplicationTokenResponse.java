package de.adorsys.multibanking.ing.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationTokenResponse extends TokenResponse {
    @JsonProperty("client_id")
    private String clientId;

    public final String getClientId() {
        return clientId;
    }
}
