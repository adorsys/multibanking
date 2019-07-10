package de.adorsys.multibanking.service;

import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@Slf4j
@Configuration
@EnableScheduling
public class DeleteExpiredUsersScheduled {

    private final UserRepositoryIf userRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccessService bankAccessService;

    @Scheduled(fixedDelay = 2 * 60 * 1000)
    void deleteJob() {
        AtomicInteger count = new AtomicInteger(0);

        userRepository.findExpiredUser().forEach(userId -> {
            bankAccessRepository.findByUserId(userId).forEach(bankAccessEntity -> bankAccessService.deleteBankAccess(userId, bankAccessEntity.getId()));
            userRepository.delete(userId);
            count.incrementAndGet();
        });

        log.info("delete job done, [{}] users deleted", count);
    }

    @Bean
    public TaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("poolScheduler");
        return scheduler;
    }

}
