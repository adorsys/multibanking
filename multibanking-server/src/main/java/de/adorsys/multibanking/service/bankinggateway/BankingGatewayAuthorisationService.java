package de.adorsys.multibanking.service.bankinggateway;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.ConsentAuthorisationRequiredException;
import de.adorsys.multibanking.exception.ConsentRequiredException;
import de.adorsys.multibanking.web.model.ConsentTO;
import de.adorsys.smartanalytics.api.config.ConfigStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static de.adorsys.multibanking.domain.ScaStatus.*;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@RequiredArgsConstructor
@Service
public class BankingGatewayAuthorisationService {

    private final BankingGatewayMapper bankingGatewayMapper;
    @Autowired
    @Qualifier("bankinggateway")
    private RestTemplate bankinggatewayRestTemplate;
    @Value("${bankinggateway.auth.url}")
    private String consentAuthUrl;

    public void checkForValidConsent(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService) {
        if (onlineBankingService.psd2Scope()) {
            Consent consent = Optional.ofNullable(bankAccess.getPsd2ConsentId())
                .map(this::getConsent)
                .orElseThrow(ConsentRequiredException::new);

            if (consent.getScaStatus() != VALID) {
                if (consent.getScaStatus() == RECEIVED || consent.getScaStatus() == PARTIALLY_AUTHORISED) {
                    String authUrl = consent.getRedirectUrl() == null
                        ? fromHttpUrl(consentAuthUrl).buildAndExpand(consent.getConsentId(),
                        consent.getConsentAuthorisationId()).toUriString()
                        : null;
                    throw new ConsentAuthorisationRequiredException(consent, authUrl);
                } else {
                    throw new ConsentRequiredException();
                }
            }
        }
    }

    public Consent getConsent(String consentId) {
        ResponseEntity<Consent> responseEntity = bankinggatewayRestTemplate
            .getForEntity("/{consentId}", Consent.class, consentId);

        return null;
    }

    public Consent createConsent(ConsentTO consent) {
        //TODO banking gateway b2c integration
        return null;
    }

    public void revokeConsent(String consentId) {
        //TODO banking gateway b2c integration
    }
}
