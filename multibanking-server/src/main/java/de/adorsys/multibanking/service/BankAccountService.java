package de.adorsys.multibanking.service;

import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.AccountInformationTransaction;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static de.adorsys.multibanking.domain.transaction.AbstractScaTransaction.TransactionType.LOAD_BANKACCOUNTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService extends AccountInformationService {

    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final ConsentService consentService;
    private final UserService userService;
    private final BankService bankService;
    private final FinTSProductConfig finTSProductConfig;

    public List<BankAccountEntity> getBankAccounts(String userId, String accessId, @Nullable Credentials credentials) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(userId, accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        List<BankAccountEntity> bankAccounts = bankAccountRepository.findByUserIdAndBankAccessId(userId, accessId);

        if (bankAccounts.isEmpty()) {
            bankAccounts = loadBankAccountsOnline(bankAccessEntity, null, credentials);
            bankAccounts.forEach(account -> account.setBankAccessId(bankAccessEntity.getId()));

            bankAccountRepository.save(bankAccounts);
            log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(),
                bankAccessEntity.getId());

            bankAccessRepository.save(bankAccessEntity);
        }
        return bankAccounts;
    }

    List<BankAccountEntity> loadBankAccountsOnline(BankAccessEntity bankAccess, BankApi bankApi) {
        return loadBankAccountsOnline(bankAccess, bankApi, null);
    }

    List<BankAccountEntity> loadBankAccountsOnline(BankAccessEntity bankAccess, BankApi bankApi,
                                                   @Nullable Credentials credentials) {
        return loadBankAccountsOnline(bankAccess, userService.findUser(bankAccess.getUserId()), bankApi, FINALISED,
            credentials);
    }

    public List<BankAccountEntity> loadBankAccountsOnline(BankAccessEntity bankAccess, UserEntity userEntity,
                                                          BankApi bankApi, ScaStatus expectedConsentStatus,
                                                          @Nullable Credentials credentials) {
        OnlineBankingService onlineBankingService = bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankEntity bankEntity = checkBankSupported(onlineBankingService, bankAccess.getBankCode());
        BankApiUser bankApiUser = userService.checkApiRegistration(onlineBankingService, userEntity);

        List<BankAccount> bankAccounts = loadBankAccountsOnline(expectedConsentStatus, bankAccess,
            onlineBankingService, bankApiUser, bankEntity, credentials);

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

    private List<BankAccount> loadBankAccountsOnline(ScaStatus expectedConsentStatus, BankAccessEntity bankAccess,
                                                     OnlineBankingService onlineBankingService,
                                                     BankApiUser bankApiUser, BankEntity bankEntity,
                                                     Credentials credentials) {
        Optional<ConsentEntity> consentEntity = consentService.validateAndGetConsent(onlineBankingService,
            bankAccess.getConsentId(),
            expectedConsentStatus);

        LoadAccountInformationRequest request = new LoadAccountInformationRequest();
        request.setBankUrl(bankEntity.getBankingUrl());
        request.setTransaction(new AccountInformationTransaction(LOAD_BANKACCOUNTS));
        request.setBankApiUser(bankApiUser);
        request.setBankAccess(bankAccess);
        request.setBankCode(bankEntity.getBankApiBankCode());
        request.setCredentials(credentials);
        request.setUpdateTanTransportTypes(true);
        request.setHbciProduct(finTSProductConfig.getProduct());
        consentEntity.ifPresent(consent -> request.setConsentId(consent.getId()));

        try {
            return onlineBankingService.loadBankAccounts(request).getBankAccounts();
        } catch (MultibankingException e) {
            throw handleMultibankingException(bankAccess, e);
        }
    }

    private BankEntity checkBankSupported(OnlineBankingService onlineBankingService, String bankCode) {
        if (!onlineBankingService.bankSupported(bankCode)) {
            throw new InvalidBankAccessException(bankCode);
        }
        return bankService.findBank(bankCode);
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
