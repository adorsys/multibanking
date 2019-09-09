package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class BankAccessService {

    private final AnalyticsRepositoryIf analyticsRepository;
    private final ContractRepositoryIf contractRepository;
    private final StandingOrderRepositoryIf standingOrderRepository;
    private final UserRepositoryIf userRepository;
    private final UserService userService;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BookingRepositoryIf bookingRepository;
    private final BankAccountService bankAccountService;
    private final BankService bankService;
    private final ConsentRepositoryIf consentRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;

    public BankAccessEntity createBankAccess(BankAccessEntity bankAccess) {
        userService.checkUserExists(bankAccess.getUserId());

        BankEntity bank = bankService.findBank(bankAccess.getBankCode());
        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bank, bankAccess, null);

        if (bankAccounts.isEmpty()) {
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }

        bankAccess.setBankName(bank.getName());
        bankAccessRepository.save(bankAccess);

        bankAccounts.forEach(account -> account.setBankAccessId(bankAccess.getId()));
        bankAccountRepository.save(bankAccounts);

        log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(), bankAccess.getId());
        return bankAccess;
    }

    public void updateBankAccess(String accessId, BankAccessEntity bankAccessEntity) {
        BankAccessEntity bankAccessEntityDb = bankAccessRepository.findByUserIdAndId(bankAccessEntity.getUserId(),
            accessId).orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        bankAccessEntityDb.setStoreBookings(bankAccessEntity.isStoreBookings());
        bankAccessEntityDb.setCategorizeBookings(bankAccessEntity.isCategorizeBookings());
        bankAccessEntityDb.setStoreAnalytics(bankAccessEntity.isStoreAnalytics());
        bankAccessEntityDb.setStoreAnonymizedBookings(bankAccessEntity.isStoreAnonymizedBookings());

        if (!bankAccessEntityDb.isStoreBookings() || !bankAccessEntityDb.isStoreAnalytics()) {
            bankAccountRepository.findByUserIdAndBankAccessId(bankAccessEntityDb.getUserId(),
                bankAccessEntityDb.getId()).forEach(bankAccountEntity -> {
                if (!bankAccessEntityDb.isStoreBookings()) {
                    bookingRepository.deleteByAccountId(bankAccountEntity.getId());
                }
                if (!bankAccessEntityDb.isStoreAnalytics()) {
                    analyticsRepository.deleteByAccountId(bankAccountEntity.getId());
                }
            });
        }
        bankAccessRepository.save(bankAccessEntityDb);
    }

    @Transactional
    public boolean deleteBankAccess(String userId, String accessId) {
        return bankAccessRepository.findByUserIdAndId(userId, accessId).map(bankAccessEntity -> {
            bankAccessRepository.deleteByUserIdAndBankAccessId(userId, accessId);

            deleteConsent(bankAccessEntity.getConsentId());

            List<BankAccountEntity> bankAccounts = bankAccountRepository.deleteByBankAccess(accessId);
            bankAccounts.forEach(bankAccountEntity -> {
                bookingRepository.deleteByAccountId(bankAccountEntity.getId());
                analyticsRepository.deleteByAccountId(bankAccountEntity.getId());
                contractRepository.deleteByAccountId(bankAccountEntity.getId());
                standingOrderRepository.deleteByAccountId(bankAccountEntity.getId());
                deleteExternalBankAccount(userId, bankAccountEntity);
            });
            return true;
        }).orElse(false);
    }

    private void deleteExternalBankAccount(String userId, BankAccountEntity bankAccountEntity) {
        bankAccountEntity.getExternalIdMap().keySet().forEach(bankApi -> {
            OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankApi);
            //remove remote bank api user
            if (bankingService.userRegistrationRequired()) {
                UserEntity userEntity =
                    userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(UserEntity.class,
                        userId));
                BankApiUser bankApiUser =
                    userEntity.getApiUser().stream().filter(apiUser -> apiUser.getBankApi() == bankApi).findFirst().orElseThrow(() -> new ResourceNotFoundException(BankApiUser.class, bankApi.toString()));
                bankingService.removeBankAccount(bankAccountEntity, bankApiUser);
            }
        });
    }

    private void deleteConsent(String consentId) {
        Optional.ofNullable(consentId)
            .map(consentRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .ifPresent(internalConsent -> {
                OnlineBankingService bankingService =
                    bankingServiceProducer.getBankingService(internalConsent.getBankApi());
                bankingService.getStrongCustomerAuthorisation().revokeConsent(internalConsent.getId());
                consentRepository.delete(internalConsent);
            });

    }

}
