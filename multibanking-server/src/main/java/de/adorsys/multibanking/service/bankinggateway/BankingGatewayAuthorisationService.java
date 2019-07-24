package de.adorsys.multibanking.service.bankinggateway;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.ConsentAuthorisationRequiredException;
import de.adorsys.multibanking.exception.ConsentRequiredException;
import de.adorsys.multibanking.web.model.ConsentTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.multibanking.domain.ScaStatus.VALID;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@RequiredArgsConstructor
@Service
public class BankingGatewayAuthorisationService {

    private final BankingGatewayMapper bankingGatewayMapper;

    @Value("${consent.auth.url}")
    private String consentAuthUrl;

    public void checkForValidConsent(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService) {
        if (onlineBankingService.psd2Scope()) {
            Consent consent = Optional.ofNullable(bankAccess.getPsd2ConsentId())
                .map(this::getConsent)
                .orElseThrow(ConsentRequiredException::new);

            if (consent.getScaStatus() != VALID) {
                String authUrl = consent.getRedirectUrl() == null
                    ? fromHttpUrl(consentAuthUrl).buildAndExpand(consent.getConsentId(),
                    consent.getConsentAuthorisationId()).toUriString()
                    : null;
                throw new ConsentAuthorisationRequiredException(consent, authUrl);
            }
        }
    }

    public Consent getConsent(String consentId) {
        //TODO banking gateway b2c integration
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
