package de.adorsys.multibanking.service;

import com.nimbusds.jwt.JWTClaimsSet;
import de.adorsys.sts.keymanagement.service.DecryptionService;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecretClaimDecryptionService {

    @Value("${sts.audience_name:}")
    private String audience;
    @Value("${sts.secret_claim_property_key:}")
    private String secretClaimPropertyKey;
    @Autowired(required = false)
    private DecryptionService decryptionService;

    public String decryptSecretClaim() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("not authenticated");
            return null;
        }

        if (authentication.getCredentials() instanceof JWTClaimsSet) {
            JWTClaimsSet credentials = (JWTClaimsSet) authentication.getCredentials();
            JSONObject encryptedSecretClaims = (JSONObject) credentials.getClaim(secretClaimPropertyKey);
            String encryptedSecretClaim = encryptedSecretClaims.getAsString(audience);

            if (encryptedSecretClaim == null) {
                log.warn("missing secret claim");
                return null;
            }

            return decryptionService.decrypt(encryptedSecretClaim);
        }

        return null;
    }
}
