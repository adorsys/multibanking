package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.web.mapper.AnalyticsMapper;
import de.adorsys.multibanking.web.model.AnalyticsTO;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Timed("analytics")
@Api(tags = "Multibanking analytics")
@RequiredArgsConstructor
@UserResource
@RestController
@RequestMapping(path = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/analytics")
public class BankAccountAnalyticsController {

    private final AnalyticsMapper analyticsMapper;
    private final AnalyticsRepositoryIf analyticsRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final Principal principal;

    @ApiOperation(
        value = "Read account analytics",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping
    public Resource<AnalyticsTO> getAccountAnalytics(@PathVariable String accessId,
                                                     @PathVariable String accountId) {
        if (!bankAccessRepository.exists(accessId)) {
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
        }
        if (!bankAccountRepository.exists(accountId)) {
            throw new ResourceNotFoundException(BankAccountEntity.class, accountId);
        }
        if (bankAccountRepository.getSyncStatus(accountId) == BankAccount.SyncStatus.SYNC) {
            throw new SyncInProgressException(accountId);
        }

        AccountAnalyticsEntity accountAnalyticsEntity = analyticsRepository
            .findLastByUserIdAndAccountId(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(
                AccountAnalyticsEntity.class,
                "user-id: " + principal.getName()
            ));

        return new Resource<>(analyticsMapper.toAnalyticsTO(accountAnalyticsEntity));
    }
}
