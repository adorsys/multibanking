package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.mapper.BankAccountMapper;
import de.adorsys.multibanking.web.model.BankAccountTO;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking bankaccount")
@Slf4j
@UserResource
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/bankaccesses/{accessId}/accounts")
public class BankAccountController {

    private final BankAccountMapper bankAccountMapper;
    private final BankAccountService bankAccountService;
    private final BookingService bookingService;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final Principal principal;

    @ApiOperation(
        value = "Read bank accounts",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @ApiResponses({
        @ApiResponse(code = 400, message = "Consent authorisation required", response = Messages.class)})
    @GetMapping
    public Resources<Resource<BankAccountTO>> getBankAccounts(@PathVariable String accessId) {
        List<BankAccountEntity> bankAccounts = bankAccountService.getBankAccounts(principal.getName(), accessId);
        return new Resources<>(mapToResources(bankAccounts, accessId));
    }

    @ApiOperation(
        value = "Read bank account",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping("/{accountId}")
    public Resource<BankAccountTO> getBankAccount(@PathVariable String accessId,
                                                  @PathVariable("accountId") String accountId) {
        if (!bankAccessRepository.exists(accessId)) {
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
        }

        BankAccountEntity bankAccountEntity = bankAccountRepository.findByUserIdAndId(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        return mapToResource(bankAccountEntity, accessId);
    }

    @ApiOperation(
        value = "Trigger account sync",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @ApiResponses({
        @ApiResponse(code = 204, message = "Sync started", response = void.class)})
    @PutMapping("/{accountId}/sync")
    public ResponseEntity syncBookings(
        @PathVariable String accessId,
        @PathVariable String accountId,
        @RequestBody(required = false) String pin) {

        BankAccessEntity bankAccess = bankAccessRepository.findByUserIdAndId(principal.getName(), accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        BankAccountEntity bankAccount = bankAccountRepository.findByUserIdAndId(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        if (bankAccount.getSyncStatus() == BankAccount.SyncStatus.SYNC) {
            throw new SyncInProgressException(bankAccount.getId());
        }
        bookingService.syncBookings(FINALISED, bankAccess, bankAccount, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<Resource<BankAccountTO>> mapToResources(List<BankAccountEntity> accountEntities, String accessId) {
        return accountEntities.stream()
            .map(accountEntity -> mapToResource(accountEntity, accessId))
            .collect(toList());
    }

    private Resource<BankAccountTO> mapToResource(BankAccountEntity accountEntity, String accessId) {
        return new Resource<>(bankAccountMapper.toBankAccountTO(accountEntity),
            linkTo(methodOn(BankAccountController.class).getBankAccount(accessId, accountEntity.getId())).withSelfRel(),
            linkTo(methodOn(BankAccessController.class).getBankAccess(accessId)).withRel("bankAccess"),
            linkTo(methodOn(BankAccountAnalyticsController.class).getAccountAnalytics(accessId,
                accountEntity.getId())).withRel("analytics"),
            linkTo(methodOn(BankAccountController.class).syncBookings(accessId, accountEntity.getId(), null)).withRel("sync"),
            linkTo(methodOn(BookingController.class).getBookings(accessId, accountEntity.getId(), null, null, null,
                null)).withRel("bookings"));
    }

}
