package de.adorsys.multibanking.service;

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

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepositoryIf userRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;

    BankApiUser checkApiRegistration(OnlineBankingService onlineBankingService, UserEntity userEntity) {
        if (onlineBankingService.userRegistrationRequired()) {
            return userEntity.getApiUser()
                .stream()
                .filter(bankApiUser -> bankApiUser.getBankApi() == onlineBankingService.bankApi())
                .findFirst()
                .orElseGet(() -> registerUser(userEntity.getId(), onlineBankingService, userEntity));
        } else {
            BankApiUser bankApiUser = new BankApiUser();
            bankApiUser.setBankApi(onlineBankingService.bankApi());
            return bankApiUser;
        }
    }

    private BankApiUser registerUser(String userId, OnlineBankingService onlineBankingService,
                                     UserEntity userEntity) {
        BankApiUser bankApiUser = onlineBankingService.registerUser(userId);
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

    UserEntity findUser(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(UserEntity.class, userId));
    }

    Optional<LocalDateTime> getRulesLastChangeDate(String userId) {
        return userRepository.getRulesLastChangeDate(userId);
    }
}
