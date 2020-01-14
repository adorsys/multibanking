package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.ContractRepositoryIf;
import de.adorsys.multibanking.web.mapper.ContractMapper;
import de.adorsys.multibanking.web.model.ContractTO;
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
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Timed("contract")
@Tag(name = "Contract")
@RequiredArgsConstructor
@UserResource
@RestController
@RequestMapping(path = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/contracts")
public class ContractController {

    private final ContractMapper contractMapper;
    private final ContractRepositoryIf contractRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final Principal principal;

    @Operation(description = "Read account contracts", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping
    public Resources<ContractTO> getContracts(@PathVariable String accessId, @PathVariable String accountId) {
        if (!bankAccessRepository.exists(accessId)) {
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
        }
        if (!bankAccountRepository.exists(accountId)) {
            throw new ResourceNotFoundException(BankAccountEntity.class, accountId);
        }
        if (bankAccountRepository.getSyncStatus(accountId) == BankAccount.SyncStatus.SYNC) {
            throw new SyncInProgressException(accountId);
        }

        List<ContractEntity> contractEntities = contractRepository.findByUserIdAndAccountId(principal.getName(),
            accountId);

        return new Resources<>(
            contractMapper.toContractTOs(contractEntities),
            linkTo(methodOn(ContractController.class).getContracts(accessId, accountId)).withSelfRel()
        );
    }
}
