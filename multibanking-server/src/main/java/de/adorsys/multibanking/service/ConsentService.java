package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationSelectionException;
import de.adorsys.multibanking.exception.MissingConsentException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.TransactionAuthorisationRequiredException;
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

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ConsentService {

    private final ConsentRepositoryIf consentRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    private final ConsentMapper consentMapper;
    private final BankService bankService;

    public CreateConsentResponse createConsent(Consent consent, String tppRedirectUri, BankApi bankApi) {
        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getPsuAccountIban());
        BankEntity bank = bankService.findBank(Iban.valueOf(consent.getPsuAccountIban()).getBankCode());

        CreateConsentResponse createConsentResponse =
            onlineBankingService.getStrongCustomerAuthorisation().createConsent(consent, bank.isRedirectPreferred(),
                tppRedirectUri);

        ConsentEntity consentEntity = consentMapper.toConsentEntity(createConsentResponse, consent.getRedirectId(), consent.getPsuAccountIban(),
            onlineBankingService.bankApi());
        consentRepository.save(consentEntity);

        createConsentResponse.setRedirectId(consent.getRedirectId());
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
                internalConsent, bank);

        UpdateAuthResponse response =
            onlineBankingService.getStrongCustomerAuthorisation().updatePsuAuthentication(updatePsuAuthenticationRequest);

        internalConsent.setBankApiConsentData(updatePsuAuthenticationRequest.getBankApiConsentData());
        consentRepository.save(internalConsent);
        return response;
    }

    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequestTO, String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethodRequest =
            consentAuthorisationMapper.toSelectPsuAuthenticationMethodRequest(selectPsuAuthenticationMethodRequestTO,
                internalConsent);

        UpdateAuthResponse response =
            onlineBankingService.getStrongCustomerAuthorisation().selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest);
        internalConsent.setBankApiConsentData(selectPsuAuthenticationMethodRequest.getBankApiConsentData());
        consentRepository.save(internalConsent);
        return response;
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

        UpdateAuthResponse response =
            onlineBankingService.getStrongCustomerAuthorisation().authorizeConsent(transactionAuthorisationRequest);
        internalConsent.setBankApiConsentData(transactionAuthorisationRequest.getBankApiConsentData());
        consentRepository.save(internalConsent);
        return response;
    }

    public void revokeConsent(String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        onlineBankingService.getStrongCustomerAuthorisation().revokeConsent(consentId);
        throw new ResourceNotFoundException(ConsentTO.class, consentId);
    }

    public ConsentEntity getInternalConsent(String consentId) {
        return consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
    }

    public Consent getConsent(String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        Consent consent = onlineBankingService.getStrongCustomerAuthorisation().getConsent(consentId);
        return Optional.ofNullable(consent)
            .orElseGet(() -> consentMapper.toConsent(internalConsent));
    }

    public Consent getConsentByRedirectId(String redirectId) {
        ConsentEntity internalConsent = consentRepository.findByRedirectId(redirectId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, redirectId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        Consent consent = onlineBankingService.getStrongCustomerAuthorisation().getConsent(internalConsent.getId());
        return Optional.ofNullable(consent)
            .orElseGet(() -> consentMapper.toConsent(internalConsent));
    }

    public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));
        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        return onlineBankingService.getStrongCustomerAuthorisation().getAuthorisationStatus(consentId,
            authorisationId, internalConsent.getBankApiConsentData());
    }

    private OnlineBankingService getOnlineBankingService(BankApi bankApi, String iban) {
        return bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(Iban.valueOf(iban).getBankCode());
    }

    ConsentEntity validateAndGetConsent(OnlineBankingService onlineBankingService, String consentId, ScaStatus expectedConsentStatus) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        try {
            onlineBankingService.getStrongCustomerAuthorisation().validateConsent(internalConsent.getId(),
                internalConsent.getAuthorisationId(),
                expectedConsentStatus, internalConsent.getBankApiConsentData());

        } catch (MultibankingException e) {
            switch (e.getMultibankingError()) {
                case INVALID_PIN:
                    throw new MissingConsentException();
                case INVALID_SCA_METHOD:
                    throw new MissingConsentAuthorisationSelectionException();
                case INVALID_CONSENT_STATUS:
                    if (expectedConsentStatus == ScaStatus.FINALISED) {
                        // TODO don't know where to get UpdateAuthResponse
                        throw new TransactionAuthorisationRequiredException(null, internalConsent.getId(),
                            internalConsent.getAuthorisationId());
                    } else if (expectedConsentStatus == ScaStatus.SCAMETHODSELECTED) {
                        throw new MissingConsentAuthorisationSelectionException();
                    }
                    throw e;
                default:
                    throw e;
            }
        }

        return internalConsent;
    }

}
