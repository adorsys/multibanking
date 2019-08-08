package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.exception.ConsentRequiredException;
import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.AccountInformationTransaction;
import de.adorsys.multibanking.exception.*;
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

import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_AUTHORISATION;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;
import static de.adorsys.multibanking.domain.transaction.AbstractScaTransaction.TransactionType.LOAD_BANKACCOUNTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final StrongCustomerAuthorisationService strongCustomerAuthorisationService;
    private final UserService userService;
    private final BankService bankService;
    private final FinTSProductConfig finTSProductConfig;

    public List<BankAccountEntity> getBankAccounts(String userId, String accessId) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(userId, accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        List<BankAccountEntity> bankAccounts = bankAccountRepository.findByUserIdAndBankAccessId(userId, accessId);

        if (bankAccounts.isEmpty()) {
            bankAccounts = loadBankAccountsOnline(bankAccessEntity, null);
            bankAccounts.forEach(account -> account.setBankAccessId(bankAccessEntity.getId()));

            bankAccountRepository.save(bankAccounts);
            log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(),
                bankAccessEntity.getId());

            bankAccessRepository.save(bankAccessEntity);
        }
        return bankAccounts;
    }

    public List<BankAccountEntity> loadBankAccountsOnline(BankAccessEntity bankAccess, BankApi bankApi) {
        OnlineBankingService onlineBankingService = bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        checkBankSupported(bankAccess, onlineBankingService);
        strongCustomerAuthorisationService.checkForValidConsent(bankAccess, onlineBankingService);

        BankApiUser bankApiUser = userService.checkApiRegistration(bankAccess, bankApi);
        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

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

    private List<BankAccount> loadBankAccountsOnline(BankAccessEntity bankAccess,
                                                     OnlineBankingService onlineBankingService,
                                                     BankApiUser bankApiUser, BankEntity bankEntity) {
        try {
            LoadAccountInformationRequest request = LoadAccountInformationRequest.builder()
                .bankUrl(bankEntity.getBankingUrl())
                .consentId(bankAccess.getPsd2ConsentId())
                .transaction(new AccountInformationTransaction(LOAD_BANKACCOUNTS))
                .bankApiUser(bankApiUser)
                .bankAccess(bankAccess)
                .bankCode(bankEntity.getBlzHbci())
                .pin(bankAccess.getPin())
                .storePin(bankAccess.isStorePin())
                .updateTanTransportTypes(true)
                .build();
            request.setProduct(finTSProductConfig.getProduct());
            return onlineBankingService.loadBankAccounts(request)
                .getBankAccounts();
        } catch (MultibankingException e) {
            return handleMultibankingException(bankAccess, e);
        }
    }

    private List<BankAccount> handleMultibankingException(BankAccessEntity bankAccess, MultibankingException e) {
        if (e.getMultibankingError() == INVALID_PIN) {
            bankAccess.setPin(null);
            bankAccessRepository.save(bankAccess);
            throw new InvalidPinException(bankAccess.getId());
        } else if (e.getMultibankingError() == INVALID_AUTHORISATION) {
            bankAccess.setAuthorisation(null);
            bankAccessRepository.save(bankAccess);
            throw new MissingAuthorisationException();
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
