package de.adorsys.multibanking.service.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import de.adorsys.sts.keymanagement.service.DecryptionService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
public class SecretClaimDecryptionService {

    private static final TypeReference<Map<String, String>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, String>>() {
    };

    private final String audience;
    private final String secretClaimPropertyKey;

    private final DecryptionService decryptionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecretClaimDecryptionService(
            @Value("${sts.audience_name}") String audience,
            @Value("${sts.secret_claim_property_key}") String secretClaimPropertyKey,
            DecryptionService decryptionService
    ) {
        this.audience = audience;
        this.secretClaimPropertyKey = secretClaimPropertyKey;
        this.decryptionService = decryptionService;
    }


    public String decryptSecretClaim() {
        Map<String, String> encryptedSecretClaims;
        try {
            encryptedSecretClaims = readSecretClaims();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String encryptedSecretClaim = encryptedSecretClaims.get(audience);
        if(StringUtils.isNotBlank(encryptedSecretClaim))
        	return decryptionService.decrypt(encryptedSecretClaim);
        return null;
    }

    private Map<String, String> readSecretClaims() throws IOException {
    	JWTClaimsSet credentials = null;
    	Object credentialsObject = SecurityContextHolder.getContext().getAuthentication().getCredentials();
    	if(credentialsObject instanceof JWTClaimsSet){
    		credentials = (JWTClaimsSet)credentialsObject ;
    	} else {
    		return Collections.emptyMap();
    	}

        String secretClaim = (String)credentials.getClaim(secretClaimPropertyKey);

        return objectMapper.readValue(secretClaim, MAP_TYPE_REFERENCE);
    }
}
