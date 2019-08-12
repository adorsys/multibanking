package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
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
import de.adorsys.multibanking.web.mapper.ConsentAuthorisationMapper;
import de.adorsys.multibanking.web.mapper.ConsentMapper;
import de.adorsys.multibanking.web.model.ConsentTO;
import de.adorsys.multibanking.web.model.SelectPsuAuthenticationMethodRequestTO;
import de.adorsys.multibanking.web.model.TransactionAuthorisationRequestTO;
import de.adorsys.multibanking.web.model.UpdatePsuAuthenticationRequestTO;
import lombok.RequiredArgsConstructor;
import org.iban4j.Iban;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConsentService {

    private final ConsentRepositoryIf consentRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    private final ConsentMapper consentMapper;
    private final BankService bankService;

    public CreateConsentResponse createConsent(Consent consent, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getPsuAccountIban());
        CreateConsentResponse createConsentResponse =
            onlineBankingService.getStrongCustomerAuthorisation().createConsent(consent);

        ConsentEntity consentEntity = consentMapper.toConsentEntity(createConsentResponse, consent.getPsuAccountIban(),
            onlineBankingService.bankApi());
        consentRepository.save(consentEntity);

        return createConsentResponse;
    }

    public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequestTO, String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());
        BankEntity bank = bankService.findBank(Iban.valueOf(internalConsent.getPsuAccountIban()).getBankCode());

        UpdatePsuAuthenticationRequest updatePsuAuthenticationRequest =
            consentAuthorisationMapper.toUpdatePsuAuthenticationRequest(updatePsuAuthenticationRequestTO,
                internalConsent);

        return onlineBankingService.getStrongCustomerAuthorisation().updatePsuAuthentication(updatePsuAuthenticationRequest, bank.getBankingUrl());
    }

    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequestTO, String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethodRequest =
            consentAuthorisationMapper.toSelectPsuAuthenticationMethodRequest(selectPsuAuthenticationMethodRequestTO,
                internalConsent);

        return onlineBankingService.getStrongCustomerAuthorisation().selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest);
    }

    public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequestTO transactionAuthorisationRequestTO,
                                               String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        TransactionAuthorisationRequest transactionAuthorisationRequest =
            consentAuthorisationMapper.toTransactionAuthorisationRequest(transactionAuthorisationRequestTO,
                internalConsent);

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

        return onlineBankingService.getStrongCustomerAuthorisation().getConsent(consentId);
    }

    public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        return onlineBankingService.getStrongCustomerAuthorisation().getAuthorisationStatus(consentId, authorisationId, );
    }

    private OnlineBankingService getOnlineBankingService(BankApi bankApi, String iban) {
        return bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(Iban.valueOf(iban).getBankCode());
    }

    void validate(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService,
                  ScaStatus consentStatus) {
        if (onlineBankingService.getStrongCustomerAuthorisation() == null) {
            // Bank API doesn't support SCA so nothing to validate
            return;
        }
        try {
            onlineBankingService.getStrongCustomerAuthorisation().validateConsent(bankAccess.getConsentId(),
                consentStatus);
        } catch (MultibankingException e) {
            switch (e.getMultibankingError()) {
                case INVALID_PIN:
                    throw new MissingConsentException();
                case INVALID_SCA_METHOD:
                    throw new MissingConsentAuthorisationSelectionException();
                case INVALID_TAN:
                    // FIXME get the challenge data
                    ChallengeData challengeData = null;
                    UpdateAuthResponse response = new UpdateAuthResponse();
                    response.setChallenge(challengeData);
                    response.setPsuMessage(e.getMessage());
                    throw new MissingConsentAuthorisationException(response, bankAccess.getConsentId(),
                        bankAccess.getAuthorisationId());
                default:
                    throw e;
            }
        }
    }

}
