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
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.TransactionAuthorisationRequiredException;
import de.adorsys.multibanking.metrics.MetricsCollector;
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
    private final MetricsCollector metricsCollector;

    public CreateConsentResponse createConsent(Consent consent, String tppRedirectUri, BankApi bankApi) {
        Object bankApiConsentData = Optional.ofNullable(consent.getConsentId())
            .flatMap(consentRepository::findById)
            .map(ConsentEntity::getBankApiConsentData)
            .orElse(null);

        OnlineBankingService onlineBankingService = getOnlineBankingService(bankApi, consent.getPsuAccountIban());
        BankEntity bank = bankService.findBank(Iban.valueOf(consent.getPsuAccountIban()).getBankCode());

        try {
            CreateConsentResponse createConsentResponse =
                onlineBankingService.getStrongCustomerAuthorisation().createConsent(consent, bank.isRedirectPreferred(),
                    tppRedirectUri, bankApiConsentData);
            createConsentResponse.setRedirectId(consent.getRedirectId());

            ConsentEntity consentEntity = consentMapper.toConsentEntity(createConsentResponse, consent.getRedirectId(),
                consent.getPsuAccountIban(), onlineBankingService.bankApi());
            consentRepository.save(consentEntity);

            metricsCollector.count("createConsent", bank.getBankCode(), onlineBankingService.bankApi());

            return createConsentResponse;
        } catch (Exception e) {
            metricsCollector.count("createConsent", bank.getBankCode(), onlineBankingService.bankApi(), e);
            throw e;
        }
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

        try {
            UpdateAuthResponse response =
                onlineBankingService.getStrongCustomerAuthorisation().updatePsuAuthentication(updatePsuAuthenticationRequest);

            internalConsent.setBankApiConsentData(updatePsuAuthenticationRequest.getBankApiConsentData());
            consentRepository.save(internalConsent);

            metricsCollector.count("updatePsuAuthentication", bank.getBankCode(), onlineBankingService.bankApi());

            return response;
        } catch (Exception e) {
            metricsCollector.count("updatePsuAuthentication", bank.getBankCode(), onlineBankingService.bankApi(), e);
            throw e;
        }
    }

    public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequestTO, String consentId) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethodRequest =
            consentAuthorisationMapper.toSelectPsuAuthenticationMethodRequest(selectPsuAuthenticationMethodRequestTO,
                internalConsent);

        try {

            UpdateAuthResponse response =
                onlineBankingService.getStrongCustomerAuthorisation().selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest);
            internalConsent.setBankApiConsentData(selectPsuAuthenticationMethodRequest.getBankApiConsentData());
            consentRepository.save(internalConsent);

            metricsCollector.count("selectPsuAuthenticationMethod",
                Iban.valueOf(internalConsent.getPsuAccountIban()).getBankCode(), onlineBankingService.bankApi());

            return response;
        } catch (Exception e) {
            metricsCollector.count("selectPsuAuthenticationMethod",
                Iban.valueOf(internalConsent.getPsuAccountIban()).getBankCode(), onlineBankingService.bankApi(), e);
            throw e;
        }
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

        try {
            UpdateAuthResponse response =
                onlineBankingService.getStrongCustomerAuthorisation().authorizeConsent(transactionAuthorisationRequest);
            internalConsent.setBankApiConsentData(transactionAuthorisationRequest.getBankApiConsentData());
            consentRepository.save(internalConsent);

            metricsCollector.count("authorizeConsent", Iban.valueOf(internalConsent.getPsuAccountIban()).getBankCode(),
                onlineBankingService.bankApi());

            return response;
        } catch (Exception e) {
            metricsCollector.count("authorizeConsent", Iban.valueOf(internalConsent.getPsuAccountIban()).getBankCode(),
                onlineBankingService.bankApi(), e);
            throw e;
        }

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

    public void submitAuthorisationCode(String consentId, String authorisationCode) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        OnlineBankingService onlineBankingService =
            bankingServiceProducer.getBankingService(internalConsent.getBankApi());

        onlineBankingService.getStrongCustomerAuthorisation().submitAuthorisationCode(internalConsent.getBankApiConsentData(), authorisationCode);

        consentRepository.save(internalConsent);
    }

    private OnlineBankingService getOnlineBankingService(BankApi bankApi, String iban) {
        return bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(Iban.valueOf(iban).getBankCode());
    }

    ConsentEntity validateAndGetConsent(OnlineBankingService onlineBankingService, String consentId,
                                        ScaStatus expectedConsentStatus) {
        ConsentEntity internalConsent = consentRepository.findById(consentId)
            .orElseThrow(() -> new ResourceNotFoundException(ConsentEntity.class, consentId));

        try {
            onlineBankingService.getStrongCustomerAuthorisation().validateConsent(internalConsent.getId(),
                internalConsent.getAuthorisationId(), expectedConsentStatus, internalConsent.getBankApiConsentData());

        } catch (MultibankingException e) {
            switch (e.getMultibankingError()) {
                case INVALID_SCA_METHOD:
                    throw new MissingConsentAuthorisationSelectionException();
                case INVALID_CONSENT_STATUS:
                    if (expectedConsentStatus == ScaStatus.FINALISED) {
                        UpdateAuthResponse authorisationStatus = getAuthorisationStatus(internalConsent.getId(),
                            internalConsent.getAuthorisationId());
                        throw new TransactionAuthorisationRequiredException(authorisationStatus,
                            internalConsent.getId(),
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
