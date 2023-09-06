package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.TransactionAuthorisationRequiredException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.web.mapper.*;
import de.adorsys.multibanking.web.model.*;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.multibanking.domain.ScaStatus.SCAMETHODSELECTED;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Timed("direct-access2")
@Tag(name = "Direct access v2")
@UserResource
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "api/v2/direct")
public class DirectAccessControllerV2 {

    private final BookingMapper bookingMapper;
    private final BankApiMapper bankApiMapper;
    private final BalancesMapper balancesMapper;
    private final BankAccessMapper bankAccessMapper;
    private final BankAccountMapper bankAccountMapper;
    private final BankAccountService bankAccountService;
    private final BankService bankService;
    private final BookingService bookingService;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final UserRepositoryIf userRepository;
    private final ConsentService consentService;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @Operation(description = "read bank accounts")
    @ApiResponse(responseCode = "200", description = "Accounts response", content = {
        @Content(schema = @Schema(implementation = LoadBankAccountsResponse.class))
    })
    @ApiResponse(responseCode = "202", description = "Challenge response", content = {
        @Content(schema = @Schema(ref = "#/components/schemas/ResourceConsentAuthorisationResponse"))
    })
    @PostMapping("/accounts")
    public ResponseEntity loadAccounts(@Valid @RequestBody LoadAccountsRequest loadAccountsRequest,
                                       @RequestParam(required = false) BankApiTO bankApi) {
        try {
            return doLoadBankAccounts(loadAccountsRequest, bankApi);
        } catch (TransactionAuthorisationRequiredException e) {
            log.debug("process finished < return challenge");
            return createChallengeResponse(e.getResponse(), e.getConsentId(), e.getAuthorisationId());
        }
    }

    @Operation(description = "read bookings")
    @ApiResponse(responseCode = "200", description = "Bookings response", content = {
        @Content(schema = @Schema(implementation = LoadBookingsResponse.class))
    })
    @ApiResponse(responseCode = "202", description = "Challenge response", content = {
        @Content(schema = @Schema(ref = "#/components/schemas/ResourceConsentAuthorisationResponse"))
    })
    @PostMapping("/bookings")
    public ResponseEntity loadTransactions(@Valid @RequestBody LoadBookingsRequest loadBookingsRequest,
                                           @RequestParam(required = false) BankApiTO bankApi) {
        try {
            return doLoadBookings(loadBookingsRequest, bankApi);
        } catch (TransactionAuthorisationRequiredException e) {
            log.debug("process finished < return challenge");
            return createChallengeResponse(e.getResponse(), e.getConsentId(), e.getAuthorisationId());
        }
    }

    private ResponseEntity<LoadBankAccountsResponse> doLoadBankAccounts(LoadAccountsRequest loadAccountsRequest,
                                                                        BankApiTO bankApi) {
        UserEntity userEntity = createTemporaryUser();
        BankAccessEntity bankAccessEntity = prepareBankAccess(loadAccountsRequest.getBankAccess(), userEntity);
        BankEntity bankEntity = bankService.findBank(bankAccessEntity.getBankCode());

        log.debug("load bank account list from bank");
        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankEntity, bankAccessEntity,
            userEntity, bankApiMapper.toBankApi(bankApi), SCAMETHODSELECTED);

        //persisting externalId for further request
        log.debug("save bank account list to db");
        bankAccounts.forEach(account -> {
            account.setUserId(userEntity.getId());
            account.setBankAccessId(bankAccessEntity.getId());
            bankAccountRepository.save(account);
        });

        return createLoadBankAccountsResponse(bankAccounts);
    }

    private ResponseEntity<LoadBookingsResponse> doLoadBookings(LoadBookingsRequest loadBookingsRequest,
                                                                BankApiTO bankApi) {
        log.debug("process start > load booking list");
        BankAccessEntity bankAccessEntity = getBankAccessEntity(loadBookingsRequest);
        BankAccountEntity bankAccountEntity = getBankAccountEntity(loadBookingsRequest, bankAccessEntity);

        log.debug("load booking list from bank");
        List<BookingEntity> bookingEntities = bookingService.syncBookings(SCAMETHODSELECTED,
            loadBookingsRequest.getAuthorisationCode(), bankAccessEntity,
            bankAccountEntity, bankApiMapper.toBankApi(bankApi));

        return createLoadBookingsResponse(bankAccountEntity, bookingEntities);
    }

    private BankAccessEntity getBankAccessEntity(LoadBookingsRequest loadBookingsRequest) {
        return Optional.ofNullable(loadBookingsRequest.getUserId())
            .map(userId -> bankAccessRepository.findByUserIdAndId(loadBookingsRequest.getUserId(),
                loadBookingsRequest.getAccessId())
                .orElseThrow(() -> new ResourceNotFoundException(BankAccessTO.class,
                    loadBookingsRequest.getAccessId())))
            .orElseGet(() -> prepareBankAccess(loadBookingsRequest.getBankAccess()));
    }

    private BankAccountEntity getBankAccountEntity(LoadBookingsRequest loadBookingsRequest,
                                                   BankAccessEntity bankAccessEntity) {
        return Optional.ofNullable(loadBookingsRequest.getAccountId())
            .map(accountId -> bankAccountRepository.findByUserIdAndId(loadBookingsRequest.getUserId()
                , loadBookingsRequest.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(BankAccountTO.class,
                    loadBookingsRequest.getAccountId()))).orElseGet(() -> {
                BankAccountEntity bankAccountEntity = new BankAccountEntity();
                bankAccountEntity.setBankAccessId(bankAccessEntity.getId());
                bankAccountEntity.setUserId(bankAccessEntity.getUserId());
                bankAccountEntity.setIban(bankAccessEntity.getIban());
                return bankAccountEntity;
            });
    }

    private BankAccessEntity prepareBankAccess(BankAccessTO bankAccess) {
        return prepareBankAccess(bankAccess, null);
    }

    private BankAccessEntity prepareBankAccess(BankAccessTO bankAccess, UserEntity userEntity) {
        log.debug("save temporary user to db");
        //temporary user, will be deleted after x minutes
        userEntity = Optional.ofNullable(userEntity).orElseGet(this::createTemporaryUser);
        userRepository.save(userEntity);

        ConsentEntity internalConsent = consentService.getInternalConsent(bankAccess.getConsentId());

        //temporary bank access, will be deleted with user
        BankAccessEntity bankAccessEntity = bankAccessMapper.toBankAccessEntity(bankAccess, userEntity.getId(), true,
            internalConsent.getPsuAccountIban());
        bankAccessEntity.setUserId(userEntity.getId());
        bankAccessRepository.save(bankAccessEntity);
        return bankAccessEntity;
    }

    private UserEntity createTemporaryUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(thresholdTemporaryData));
        return userEntity;
    }

    private ResponseEntity<LoadBankAccountsResponse> createLoadBankAccountsResponse(List<BankAccountEntity> bankAccounts) {
        LoadBankAccountsResponse response = new LoadBankAccountsResponse();
        response.setBankAccounts(bankAccountMapper.toBankAccountTOs(bankAccounts));
        log.debug("process finished < return bank account list");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<LoadBookingsResponse> createLoadBookingsResponse(BankAccountEntity bankAccountEntity,
                                                                            List<BookingEntity> bookings) {
        LoadBookingsResponse loadBookingsResponse = new LoadBookingsResponse();
        loadBookingsResponse.setBookings(bookingMapper.toBookingTOs(bookings));
        loadBookingsResponse.setBalances(balancesMapper.toBalancesReportTO(bankAccountEntity.getBalances()));
        log.debug("process finished < return booking list");
        return new ResponseEntity<>(loadBookingsResponse, HttpStatus.OK);
    }

    private ResponseEntity<EntityModel<UpdateAuthResponseTO>> createChallengeResponse(UpdateAuthResponse response,
                                                                                      String consentId,
                                                                                      String authorisationId) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(consentId,
            authorisationId)).withSelfRel());
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).transactionAuthorisation(consentId,
            authorisationId, null)).withRel("transactionAuthorisation"));
        return ResponseEntity.accepted().body(EntityModel.of(consentAuthorisationMapper.toUpdateAuthResponseTO(response), links));
    }

    @Data
    public static class LoadAccountsRequest {
        @NotNull
        @Schema(description = "Bankaccess properties")
        BankAccessTO bankAccess;
    }

    @Data
    public static class LoadBankAccountsResponse {
        List<BankAccountTO> bankAccounts;
    }

    @Data
    public static class LoadBookingsRequest {
        @Schema(description = "Conditional: authorisation code, mandated if bank using oauth approcach")
        String authorisationCode;
        @Schema(description = "Conditional: multibanking user id, mandated if bankaccess was created")
        String userId;
        @Schema(description = "Conditional: multibanking bank access id, mandated if bankaccess was created")
        String accessId;
        @Schema(description = "Conditional: multibanking bank account id, mandated if bankaccess was created")
        String accountId;
        @Schema(description = "Conditional: bankaccess properties, mandated if bankaccess was not created")
        BankAccessTO bankAccess;
    }

    @Data
    public static class LoadBookingsResponse {
        List<BookingTO> bookings;
        BalancesReportTO balances;

    }
}
