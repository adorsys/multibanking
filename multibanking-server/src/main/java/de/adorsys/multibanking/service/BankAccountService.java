package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.TransactionRequestFactory;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;

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

    public List<BankAccountEntity> getBankAccounts(String userId, String accessId) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(userId, accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        List<BankAccountEntity> bankAccounts = bankAccountRepository.findByUserIdAndBankAccessId(userId, accessId);

        if (bankAccounts.isEmpty()) {
            BankEntity bank = bankService.findBank(bankAccessEntity.getBankCode());

            bankAccounts = loadBankAccountsOnline(bank, bankAccessEntity, null);
            bankAccounts.forEach(account -> account.setBankAccessId(bankAccessEntity.getId()));

            bankAccountRepository.save(bankAccounts);
            log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(),
                bankAccessEntity.getId());

            bankAccessRepository.save(bankAccessEntity);
        }
        return bankAccounts;
    }

    List<BankAccountEntity> loadBankAccountsOnline(BankEntity bankEntity, BankAccessEntity bankAccess,
                                                   BankApi bankApi) {
        return loadBankAccountsOnline(bankEntity, bankAccess, userService.findUser(bankAccess.getUserId()), bankApi,
            FINALISED);
    }

    public List<BankAccountEntity> loadBankAccountsOnline(BankEntity bankEntity, BankAccessEntity bankAccess,
                                                          UserEntity userEntity,
                                                          BankApi bankApi, ScaStatus expectedConsentStatus) {
        OnlineBankingService onlineBankingService = bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (!onlineBankingService.bankSupported(bankAccess.getBankCode())) {
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }
        BankApiUser bankApiUser = userService.checkApiRegistration(onlineBankingService, userEntity);

        List<BankAccount> bankAccounts = loadBankAccountsOnline(expectedConsentStatus, bankAccess,
            onlineBankingService, bankApiUser, bankEntity);

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
                                                     BankApiUser bankApiUser, BankEntity bankEntity) {
        ConsentEntity consentEntity = consentService.validateAndGetConsent(onlineBankingService,
            bankAccess.getConsentId(), expectedConsentStatus);

        TransactionRequest<LoadAccounts> transactionRequest =
            TransactionRequestFactory.create(new LoadAccounts(), bankApiUser, bankAccess, bankEntity,
                consentEntity.getBankApiConsentData());

        try {
            AccountInformationResponse response = onlineBankingService.loadBankAccounts(transactionRequest);
            checkSca(response, consentEntity, onlineBankingService);
            return response.getBankAccounts();
        } catch (MultibankingException e) {
            throw handleMultibankingException(bankAccess, e);
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
