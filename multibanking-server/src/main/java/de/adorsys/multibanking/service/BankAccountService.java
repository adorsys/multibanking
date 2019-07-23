package de.adorsys.multibanking.service;

import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.*;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.service.bankinggateway.BankingGatewayAuthorisationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.multibanking.domain.ScaStatus.VALID;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_CONSENT;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;

@Slf4j
@Service
@AllArgsConstructor
public class BankAccountService {

    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final BankingGatewayAuthorisationService authorisationService;
    private final UserService userService;
    private final BankService bankService;
    private final FinTSProductConfig finTSProductConfig;

    public List<BankAccountEntity> getBankAccounts(String userId, String accessId) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(userId, accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        List<BankAccountEntity> bankAccounts = bankAccountRepository.findByUserIdAndBankAccessId(userId, accessId);

        if (bankAccounts.isEmpty()) {

            try {
                bankAccounts = loadBankAccountsOnline(bankAccessEntity, null);
                bankAccounts.forEach(account -> account.setBankAccessId(bankAccessEntity.getId()));

                bankAccountRepository.save(bankAccounts);
                log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(),
                    bankAccessEntity.getId());
            } catch (ConsentAuthorisationRequiredException e) {
                bankAccessEntity.setPsd2ConsentId(e.getConsent().getConsentId());
                bankAccessEntity.setPsd2ConsentAuthorisationId(e.getConsent().getConsentAuthorisationId());
            } finally {
                bankAccessRepository.save(bankAccessEntity);
            }
        }
        return bankAccounts;
    }

    private void checkAvailableAccountsConsent(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService
        , BankApiUser bankApiUser, BankEntity bankEntity) {
        if (onlineBankingService.psd2Scope()) {
            if (bankAccess.getPsd2ConsentId() == null) {
                Consent availableAccountsConsent =
                    authorisationService.createAvailableAccountsConsent(onlineBankingService, bankApiUser, bankAccess,
                        bankEntity);
                throw new ConsentAuthorisationRequiredException(availableAccountsConsent);
            } else {
                ScaStatus consentStatus = authorisationService.getConsentStatus(bankAccess.getPsd2ConsentId());
                if (consentStatus != VALID) {
                    throw new ConsentAuthorisationRequiredException(authorisationService.getConsent(bankAccess.getPsd2ConsentId()));
                }
            }
        }
    }

    public List<BankAccountEntity> loadBankAccountsOnline(BankAccessEntity bankAccess, BankApi bankApi) {
        OnlineBankingService onlineBankingService = bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        checkBankSupported(bankAccess, onlineBankingService);

        BankApiUser bankApiUser = userService.checkApiRegistration(bankAccess, bankApi);
        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        checkAvailableAccountsConsent(bankAccess, onlineBankingService, bankApiUser, bankEntity);

        List<BankAccount> bankAccounts = loadBankAccountsOnline(bankAccess, onlineBankingService, bankApiUser,
            bankEntity);

        if (onlineBankingService.bankApi() == BankApi.FIGO) {
            filterAccounts(bankAccess, onlineBankingService, bankAccounts);
        }
        return Optional.ofNullable(bankAccounts)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(source -> {
                BankAccountEntity target = new BankAccountEntity();
                BeanUtils.copyProperties(source, target);
                target.setUserId(bankAccess.getUserId());
                return target;
            })
            .collect(Collectors.toList());
    }

    void checkDedicatedConsent(BankAccessEntity bankAccess, BankAccountEntity bankAccount,
                               BankApiUser bankApiUser, OnlineBankingService onlineBankingService,
                               BankEntity bankEntity) {
        if (onlineBankingService.psd2Scope()) {
            if (bankAccount.getPsd2ConsentId() == null) {
                Consent dedicatedConsent = authorisationService.createDedicatedConsent(onlineBankingService,
                    bankApiUser, bankAccess, bankAccount, bankEntity);
                throw new ConsentAuthorisationRequiredException(dedicatedConsent);
            } else {
                ScaStatus consentStatus = authorisationService.getConsentStatus(bankAccount.getPsd2ConsentId());
                if (consentStatus != VALID) {
                    throw new ConsentAuthorisationRequiredException(authorisationService.getConsent(bankAccess.getPsd2ConsentId()));
                }
            }
        }
    }

    private List<BankAccount> loadBankAccountsOnline(BankAccessEntity bankAccess,
                                                     OnlineBankingService onlineBankingService,
                                                     BankApiUser bankApiUser, BankEntity bankEntity) {
        try {
            LoadAccountInformationRequest request = LoadAccountInformationRequest.builder()
                .consentId(bankAccess.getPsd2ConsentId())
                .bankApiUser(bankApiUser)
                .bankAccess(bankAccess)
                .bankCode(bankEntity.getBlzHbci())
                .pin(bankAccess.getPin())
                .storePin(bankAccess.isStorePin())
                .updateTanTransportTypes(true)
                .build();
            request.setProduct(finTSProductConfig.getProduct());
            return onlineBankingService.loadBankAccounts(bankEntity.getBankingUrl(),
                request)
                .getBankAccounts();
        } catch (MultibankingException e) {
            return handleMultibankingException(bankAccess, bankApiUser, onlineBankingService, bankEntity, e);
        }
    }

    private List<BankAccount> handleMultibankingException(BankAccessEntity bankAccess, BankApiUser bankApiUser,
                                                          OnlineBankingService onlineBankingService,
                                                          BankEntity bankEntity, MultibankingException e) {
        if (e.getMultibankingError() == INVALID_PIN) {
            bankAccess.setPin(null);
            bankAccessRepository.save(bankAccess);
            throw new InvalidPinException(bankAccess.getId());
        } else if (e.getMultibankingError() == INVALID_CONSENT) {
            Consent availableAccountsConsent =
                authorisationService.createAvailableAccountsConsent(onlineBankingService, bankApiUser, bankAccess,
                    bankEntity);
            throw new ConsentAuthorisationRequiredException(availableAccountsConsent);
        }
        throw e;
    }

    private void checkBankSupported(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService) {
        if (!onlineBankingService.bankSupported(bankAccess.getBankCode())) {
            bankAccess.setStorePin(false);
            bankAccess.setPin(null);
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }
    }

    private void filterAccounts(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService,
                                List<BankAccount> bankAccounts) {
        List<BankAccountEntity> userBankAccounts = bankAccountRepository.findByUserId(bankAccess.getUserId());
        //filter out previous created accounts
        Iterator<BankAccount> accountIterator = bankAccounts.iterator();
        while (accountIterator.hasNext()) {
            BankAccount newAccount = accountIterator.next();
            userBankAccounts.stream()
                .filter(bankAccountEntity -> {
                    String newAccountExternalID = newAccount.getExternalIdMap().get(onlineBankingService.bankApi());
                    String existingAccountExternalID =
                        bankAccountEntity.getExternalIdMap().get(onlineBankingService.bankApi());
                    return newAccountExternalID.equals(existingAccountExternalID);
                })
                .findFirst()
                .ifPresent(bankAccountEntity -> accountIterator.remove());
        }
        //all accounts created in the past
        if (bankAccounts.isEmpty()) {
            throw new BankAccessAlreadyExistException();
        }
        bankAccess.setBankName(bankAccounts.get(0).getBankName());
    }
}
