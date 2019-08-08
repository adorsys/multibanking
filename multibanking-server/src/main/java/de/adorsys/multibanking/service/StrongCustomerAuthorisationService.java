package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.domain.Consent;
import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisation;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisationContainer;
import de.adorsys.multibanking.exception.MissingStrongCustomerAuthorisationException;
import de.adorsys.multibanking.exception.ParametrizedMessageException;
import de.adorsys.multibanking.exception.StrongCustomerAuthorisationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StrongCustomerAuthorisationService {

    @Value("${bankinggateway.auth.url}")
    private String consentAuthUrl;

    public void checkForValidConsent(StrongCustomerAuthorisationContainer container, OnlineBankingService onlineBankingService) {
        StrongCustomerAuthorisable authorisationService = onlineBankingService.getStrongCustomerAuthorisation();
        if (authorisationService != null) {
            try {
                authorisationService.containsValidAuthorisation(container);
            } catch (MissingAuthorisationException e) {
                Consent consent = (Consent)e.getAuthorisation();
                if (consent == null) {
                    throw new MissingStrongCustomerAuthorisationException(e.getMessage());
                }
                consent.setAuthUrl(consentAuthUrl);
                throw new StrongCustomerAuthorisationException(consent, e.getMessage());
            }
        }
    }
}
