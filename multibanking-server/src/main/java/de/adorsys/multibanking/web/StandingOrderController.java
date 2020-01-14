package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.StandingOrderRepositoryIf;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Timed("standing-order")
@Tag(name = "Standing order")
@RequiredArgsConstructor
@UserResource
@RestController
@RequestMapping(path = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/standingorders")
public class StandingOrderController {

    private final StandingOrderRepositoryIf standingOrderRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final Principal principal;

    @Operation(description = "Read account standing orders", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping
    public Resources<StandingOrderEntity> getStandingOrders(@PathVariable String accessId,
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

        return new Resources<>(
            standingOrderRepository.findByUserIdAndAccountId(principal.getName(), accountId),
            linkTo(methodOn(StandingOrderController.class).getStandingOrders(accessId, accountId)).withSelfRel()
        );
    }
}
