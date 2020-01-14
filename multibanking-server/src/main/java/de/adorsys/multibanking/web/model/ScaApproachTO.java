package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SCA approach")
public enum ScaApproachTO {
    EMBEDDED, REDIRECT, DECOUPLED, OAUTH, OAUTH_PRESTEP
}
