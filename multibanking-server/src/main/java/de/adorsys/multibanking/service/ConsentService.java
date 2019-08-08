package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.spi.Consent;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.InvalidPinException;
import de.adorsys.multibanking.exception.MissingStrongCustomerAuthorisationException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.web.model.ConsentTO;
import lombok.RequiredArgsConstructor;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConsentService {

    @Value("${bankinggateway.auth.url}")
    private String consentAuthUrl;

    private final OnlineBankingServiceProducer bankingServiceProducer;

    public Consent createConsent(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getIban());
        return onlineBankingService.getStrongCustomerAuthorisation().createConsent(consent);
    }

    public Consent refreshConsent(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getIban());
        Consent existingConsent = onlineBankingService.getStrongCustomerAuthorisation().getConsent(consent.getConsentId());
        consent.setScaStatus(existingConsent.getScaStatus());
        return consent;
    }

    public Consent loginConsent(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getIban());
        return onlineBankingService.getStrongCustomerAuthorisation().loginConsent(consent);
    }

    public Consent authorizeConsent(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getIban());
        return onlineBankingService.getStrongCustomerAuthorisation().authorizeConsent(consent);
    }

    public Consent selectScaMethod(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getIban());
        return onlineBankingService.getStrongCustomerAuthorisation().selectScaMethod(consent);
    }

    private OnlineBankingService getOnlineBankingService(BankApi bankApi, String iban) {
        return bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(Iban.valueOf(iban).getBankCode());
    }

    public void revokeConsent(String consentId) {
        // FIXME implement
        throw new ResourceNotFoundException(ConsentTO.class, consentId);
    }

    public Consent getConsent(String consentId, String iban, BankApi bankApi) {
        // FIXME implement
        throw new ResourceNotFoundException(ConsentTO.class, consentId);
    }

    public void validate(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService) {
        try {
            onlineBankingService.getStrongCustomerAuthorisation().validateConsent(bankAccess.getConsentId());
        } catch (MultibankingException e) {
            switch (e.getMultibankingError()) {
                case INVALID_PIN:
                    throw new InvalidPinException(bankAccess.getId());
                case INVALID_SCA_METHOD:
                    // FIXME distinct exceptions
                    throw new MissingAuthorisationException();
                case INVALID_TAN:
                    // FIXME distinct exceptions
                    throw new MissingAuthorisationException();
                default:
                    throw e;
            }
        }
    }
}
