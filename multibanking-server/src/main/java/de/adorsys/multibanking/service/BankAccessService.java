package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.Iban;
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
    private final OnlineBankingServiceProducer bankingServiceProducer;

    public BankAccessEntity createBankAccess(String userId, BankAccessEntity bankAccess) {
        userService.checkUserExists(userId);

        bankAccess.setUserId(userId);
        if (StringUtils.isNoneBlank(bankAccess.getIban())) {
            bankAccess.setBankCode(Iban.valueOf(bankAccess.getIban()).getBankCode());
        }

        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankAccess, null);

        if (bankAccounts.isEmpty()) {
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }

        saveBankAccess(bankAccess);

        bankAccounts.forEach(account -> account.setBankAccessId(bankAccess.getId()));
        bankAccountRepository.save(bankAccounts);

        log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(), bankAccess.getId());
        return bankAccess;
    }

    private void saveBankAccess(BankAccessEntity bankAccess) {
        if (!bankAccess.isStorePin()) {
            bankAccess.setPin(null);
        }
        bankAccessRepository.save(bankAccess);

        log.info("Bank connection [{}] created.", bankAccess.getId());
    }

    public void updateBankAccess(String accessId, BankAccessEntity bankAccessEntity) {
        BankAccessEntity bankAccessEntityDb = bankAccessRepository.findByUserIdAndId(bankAccessEntity.getUserId(),
            accessId).orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        bankAccessEntityDb.setStorePin(bankAccessEntity.isStorePin());
        bankAccessEntityDb.setStoreBookings(bankAccessEntity.isStoreBookings());
        bankAccessEntityDb.setCategorizeBookings(bankAccessEntity.isCategorizeBookings());
        bankAccessEntityDb.setStoreAnalytics(bankAccessEntity.isStoreAnalytics());
        bankAccessEntityDb.setStoreAnonymizedBookings(bankAccessEntity.isStoreAnonymizedBookings());
        if (!bankAccessEntityDb.isStorePin()) {
            bankAccessEntityDb.setPin(null);
        } else {
            if (bankAccessEntity.getPin() == null) {
                bankAccessEntityDb.setStorePin(false);
            } else {
                bankAccessEntityDb.setPin(bankAccessEntity.getPin());
            }
        }
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
        UserEntity userEntity =
            userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(UserEntity.class, userId));

        return bankAccessRepository.findByUserIdAndId(userId, accessId).map(bankAccessEntity -> {
            bankAccessRepository.deleteByUserIdAndBankAccessId(userId, accessId);

            List<BankAccountEntity> bankAccounts = bankAccountRepository.deleteByBankAccess(accessId);
            bankAccounts.forEach(bankAccountEntity -> {
                bookingRepository.deleteByAccountId(bankAccountEntity.getId());
                analyticsRepository.deleteByAccountId(bankAccountEntity.getId());
                contractRepository.deleteByAccountId(bankAccountEntity.getId());
                standingOrderRepository.deleteByAccountId(bankAccountEntity.getId());
                bankAccountEntity.getExternalIdMap().keySet().forEach(bankApi -> {
                    OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankApi);
                    // FIXME this would mean that there is the same authorisation id for different bank apis? which feels wrong
                    // remove authorisation if needed by the bank api
                    Optional.ofNullable(bankingService.getStrongCustomerAuthorisation())
                        .ifPresent(strongCustomerAuthorisable -> strongCustomerAuthorisable.revokeAuthorisation(bankAccessEntity.getAuthorisation()));
                    //remove remote bank api user
                    if (bankingService.userRegistrationRequired()) {
                        BankApiUser bankApiUser =
                            userEntity.getApiUser().stream().filter(apiUser -> apiUser.getBankApi() == bankApi).findFirst().orElseThrow(() -> new ResourceNotFoundException(BankApiUser.class, bankApi.toString()));
                        bankingService.removeBankAccount(bankAccountEntity, bankApiUser);
                    }
                });
            });
            return true;
        }).orElse(false);
    }

}
