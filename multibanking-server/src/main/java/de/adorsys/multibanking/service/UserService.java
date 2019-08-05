package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alexg on 05.09.17.
 */
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepositoryIf userRepository;
    private final BankService bankService;
    private final OnlineBankingServiceProducer bankingServiceProducer;

    BankApiUser checkApiRegistration(BankAccessEntity bankAccess, BankApi bankApi) {
        OnlineBankingService onlineBankingService = bankApi != null
            ? bankingServiceProducer.getBankingService(bankApi)
            : bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (onlineBankingService.userRegistrationRequired()) {
            UserEntity userEntity = userRepository.findById(bankAccess.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(UserEntity.class, bankAccess.getUserId()));

            return userEntity.getApiUser()
                .stream()
                .filter(bankApiUser -> bankApiUser.getBankApi() == onlineBankingService.bankApi())
                .findFirst()
                .orElseGet(() -> registerUser(bankAccess, onlineBankingService, userEntity));
        } else {
            BankApiUser bankApiUser = new BankApiUser();
            bankApiUser.setBankApi(onlineBankingService.bankApi());
            return bankApiUser;
        }
    }

    private BankApiUser registerUser(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService,
                                     UserEntity userEntity) {
        BankApiUser bankApiUser = onlineBankingService.registerUser(bankAccess, bankAccess.getPin());
        userEntity.getApiUser().add(bankApiUser);
        userRepository.save(userEntity);
        return bankApiUser;
    }

    void checkUserExists(String userId) {
        if (!userRepository.exists(userId)) {
            UserEntity userEntity = new UserEntity();
            userEntity.setApiUser(new ArrayList<>());
            userEntity.setId(userId);
            userRepository.save(userEntity);
        }
    }

    void updataeBankApiUser(String userId, BankApiUser bankApiUser) {
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(UserEntity.class, userId));

        userEntity.setApiUser(
            userEntity.getApiUser().stream()
                .filter(bau -> bau.getBankApi() != bankApiUser.getBankApi())
                .collect(Collectors.toList())
        );

        userEntity.getApiUser().add(bankApiUser);
        userRepository.save(userEntity);
    }

    Optional<LocalDateTime> getRulesLastChangeDate(String userId) {
        return userRepository.getRulesLastChangeDate(userId);
    }
}
