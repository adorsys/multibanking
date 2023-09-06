package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.exception.TransactionAuthorisationRequiredException;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.mapper.BankAccountMapper;
import de.adorsys.multibanking.web.mapper.ConsentAuthorisationMapper;
import de.adorsys.multibanking.web.model.BankAccountTO;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Timed("bank-account")
@Tag(name = "Bankaccount")
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
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    private final Principal principal;

    @Operation(description = "Read bank accounts", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @ApiResponse(responseCode = "400", description = "Consent authorisation required", content = {
        @Content(schema = @Schema(implementation = Messages.class))
    })
    @GetMapping
    public CollectionModel<EntityModel<BankAccountTO>> getBankAccounts(@PathVariable String accessId) {
        List<BankAccountEntity> bankAccounts = bankAccountService.getBankAccounts(principal.getName(), accessId);
        return CollectionModel.of(mapToResources(bankAccounts, accessId));
    }

    @Operation(description = "Read bank account", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/{accountId}")
    public EntityModel<BankAccountTO> getBankAccount(@PathVariable String accessId,
                                                  @PathVariable("accountId") String accountId) {
        if (!bankAccessRepository.exists(accessId)) {
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
        }

        BankAccountEntity bankAccountEntity = bankAccountRepository.findByUserIdAndId(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        return mapToResource(bankAccountEntity, accessId);
    }

    @Operation(description = "Trigger account sync", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @ApiResponse(responseCode = "204", description = "Sync started", content = {
        @Content(schema = @Schema(implementation = void.class))
    })
    @ApiResponse(responseCode = "202", description = "Challenge response", content = {
        @Content(schema = @Schema(ref = "#/components/schemas/ResourceConsentAuthorisationResponse"))
    })
    @PutMapping("/{accountId}/sync")
    public ResponseEntity syncBookings(@PathVariable String accessId, @PathVariable String accountId) {
        BankAccessEntity bankAccess = bankAccessRepository.findByUserIdAndId(principal.getName(), accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        BankAccountEntity bankAccount = bankAccountRepository.findByUserIdAndId(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        if (bankAccount.getSyncStatus() == BankAccount.SyncStatus.SYNC) {
            throw new SyncInProgressException(bankAccount.getId());
        }

        try {
            bookingService.syncBookings(FINALISED, null, bankAccess, bankAccount, null);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (TransactionAuthorisationRequiredException e) {
            List<Link> links = new ArrayList<>();
            links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(e.getConsentId(),
                e.getAuthorisationId())).withSelfRel());
            links.add(linkTo(methodOn(ConsentAuthorisationController.class).transactionAuthorisation(e.getConsentId(),
                e.getAuthorisationId(), null)).withRel("transactionAuthorisation"));
            return ResponseEntity.accepted().body(EntityModel.of(consentAuthorisationMapper.toUpdateAuthResponseTO(e.getResponse()), links));
        }
    }

    private List<EntityModel<BankAccountTO>> mapToResources(List<BankAccountEntity> accountEntities, String accessId) {
        return accountEntities.stream()
            .map(accountEntity -> mapToResource(accountEntity, accessId))
            .collect(toList());
    }

    private EntityModel<BankAccountTO> mapToResource(BankAccountEntity accountEntity, String accessId) {
        return EntityModel.of(bankAccountMapper.toBankAccountTO(accountEntity),
            linkTo(methodOn(BankAccountController.class).getBankAccount(accessId, accountEntity.getId())).withSelfRel(),
            linkTo(methodOn(BankAccessController.class).getBankAccess(accessId)).withRel("bankAccess"),
            linkTo(methodOn(BankAccountAnalyticsController.class).getAccountAnalytics(accessId,
                accountEntity.getId())).withRel("analytics"),
            linkTo(methodOn(BankAccountController.class).syncBookings(accessId, accountEntity.getId())).withRel("sync"),
            linkTo(methodOn(BookingController.class).getBookings(accessId, accountEntity.getId(), null, null, null,
                null)).withRel("bookings"));
    }

}
