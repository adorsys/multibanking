package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationException;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationSelectionException;
import de.adorsys.multibanking.exception.MissingConsentException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import de.adorsys.multibanking.web.model.ConsentTO;
import lombok.RequiredArgsConstructor;
import org.iban4j.Iban;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ConsentService {

    private final ConsentRepositoryIf consentRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final BankService bankService;

    public CreateConsentResponse createConsent(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getPsuAccountIban());
        CreateConsentResponse createConsentResponse =
            onlineBankingService.getStrongCustomerAuthorisation().createConsent(consent);

        consentRepository.save(new ConsentEntity(createConsentResponse.getConsentId(),
            createConsentResponse.getAuthorisationId(),
            onlineBankingService.bankApi()));

        return createConsentResponse;
    }

    public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthenticationRequestconsent) {
        ConsentEntity internalConsent = consentRepository.findById(updatePsuAuthenticationRequestconsent.getConsentId())
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, updatePsuAuthenticationRequestconsent.getConsentId()));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());
        Consent consent = onlineBankingService.getStrongCustomerAuthorisation().getConsent(updatePsuAuthenticationRequestconsent.getConsentId());
        String bankingUrl = null;
        if (consent.getPsuAccountIban() != null) {
            BankEntity bank = bankService.findBank(Iban.valueOf(consent.getPsuAccountIban()).getBankCode());
            bankingUrl = bank.getBankingUrl();
        }
        return onlineBankingService.getStrongCustomerAuthorisation().updatePsuAuthentication(updatePsuAuthenticationRequestconsent, bankingUrl);
    }

    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethodRequest) {
        ConsentEntity internalConsent = consentRepository.findById(selectPsuAuthenticationMethodRequest.getConsentId())
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, selectPsuAuthenticationMethodRequest.getConsentId()));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());
        return onlineBankingService.getStrongCustomerAuthorisation().selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest);
    }

    public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisationRequest) {
        ConsentEntity internalConsent = consentRepository.findById(transactionAuthorisationRequest.getConsentId())
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, transactionAuthorisationRequest.getConsentId()));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());
        return onlineBankingService.getStrongCustomerAuthorisation().authorizeConsent(transactionAuthorisationRequest);
    }

    public void revokeConsent(String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        onlineBankingService.getStrongCustomerAuthorisation().revokeConsent(consentId);
        throw new ResourceNotFoundException(ConsentTO.class, consentId);
    }

    public Consent getConsent(String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        return Optional.ofNullable(onlineBankingService.getStrongCustomerAuthorisation().getConsent(consentId))
            .orElse(new Consent());
    }

    public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        return onlineBankingService.getStrongCustomerAuthorisation().getAuthorisationStatus(consentId, authorisationId);
    }

    private OnlineBankingService getOnlineBankingService(BankApi bankApi, String iban) {
        return bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(Iban.valueOf(iban).getBankCode());
    }

    public void validate(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService) {
        if (onlineBankingService.getStrongCustomerAuthorisation() == null) {
            // Bank API doesn't support SCA so nothing to validate
            return;
        }
        try {
            onlineBankingService.getStrongCustomerAuthorisation().validateConsent(bankAccess.getConsentId());
        } catch (MultibankingException e) {
            switch (e.getMultibankingError()) {
                case INVALID_PIN:
                    throw new MissingConsentException();
                case INVALID_SCA_METHOD:
                    throw new MissingConsentAuthorisationSelectionException();
                case INVALID_TAN:
                    throw new MissingConsentAuthorisationException();
                default:
                    throw e;
            }
        }
    }

}
