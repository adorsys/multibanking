package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.web.mapper.*;
import de.adorsys.multibanking.web.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
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

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static de.adorsys.multibanking.domain.ScaStatus.SCAMETHODSELECTED;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking direct access")
@UserResource
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/direct")
public class DirectAccessController {

    private final BookingMapper bookingMapper;
    private final BankApiMapper bankApiMapper;
    private final BalancesMapper balancesMapper;
    private final BankAccessMapper bankAccessMapper;
    private final BankAccountMapper bankAccountMapper;
    private final CredentialsMapper credentialsMapper;
    private final BankAccountService bankAccountService;
    private final BookingService bookingService;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final UserRepositoryIf userRepository;
    private final ConsentService consentService;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @ApiOperation(value = "create challenge for accounts")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBankAccountsResponse.class)})
    @PostMapping("/accounts")
    public ResponseEntity createHbciAccountsChallenge(@Valid @RequestBody LoadAccountsRequest loadAccountsRequest,
                                                      @RequestParam(required = false) BankApiTO bankApi) {
        try {
            selectScaMethodForConsent(loadAccountsRequest.getBankAccessTO().getConsentId(),
                loadAccountsRequest.getScaMethodId());
            return doLoadBankAccounts(loadAccountsRequest, bankApi, SCAMETHODSELECTED);
        } catch (MissingConsentAuthorisationException e) {
            log.debug("process finished < return challenge");
            return createChallengeResponse(e.getResponse(), e.getConsentId(), e.getAuthorisationId());
        }
    }

    @ApiOperation(value = "Read bank accounts")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBankAccountsResponse.class)})
    @PutMapping("/accounts")
    public ResponseEntity loadBankAccounts(@Valid @RequestBody LoadAccountsRequest loadAccountsRequest,
                                           @RequestParam(required = false) BankApiTO bankApi) {
        return doLoadBankAccounts(loadAccountsRequest, bankApi, FINALISED);
    }

    @ApiOperation(value = "create challenge for bookings")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBookingsResponse.class)})
    @PostMapping("/bookings")
    public ResponseEntity createHbciBookingsChallenge(@Valid @RequestBody LoadBookingsRequest loadBookingsRequest,
                                                      @RequestParam(required = false) BankApiTO bankApi) {
        try {
            selectScaMethodForConsent(loadBookingsRequest.getBankAccess().getConsentId(),
                loadBookingsRequest.getScaMethodId());
            return doLoadBookings(loadBookingsRequest, bankApi, SCAMETHODSELECTED);
        } catch (MissingConsentAuthorisationException e) {
            log.debug("process finished < return challenge");
            return createChallengeResponse(e.getResponse(), e.getConsentId(), e.getAuthorisationId());
        }
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBookingsResponse.class)})
    @ApiOperation(value = "Read account bookings")
    @PutMapping("/bookings")
    public ResponseEntity loadBookings(@Valid @RequestBody LoadBookingsRequest loadBookingsRequest,
                                       @RequestParam(required = false) BankApiTO bankApi) {
        return doLoadBookings(loadBookingsRequest, bankApi, FINALISED);
    }

    private ResponseEntity doLoadBankAccounts(LoadAccountsRequest loadAccountsRequest,
                                              BankApiTO bankApi, ScaStatus scaStatus) {
        UserEntity userEntity = createUser();

        BankAccessEntity bankAccessEntity =
            bankAccessMapper.toBankAccessEntity(loadAccountsRequest.getBankAccessTO(), userEntity.getId(), true);

        log.debug("load bank account list from bank");
        Credentials credentials = credentialsMapper.toCredentials(loadAccountsRequest.getCredentials());

        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            userEntity, bankApiMapper.toBankApi(bankApi), scaStatus, credentials);

        userRepository.save(userEntity);
        bankAccessRepository.save(bankAccessEntity);

        //persisting externalId for further request
        log.debug("save bank account list to db");
        bankAccounts.forEach(account -> {
            account.setUserId(userEntity.getId());
            account.setBankAccessId(bankAccessEntity.getId());
            bankAccountRepository.save(account);
        });

        return createLoadBankAccountsResponse(bankAccounts);
    }

    private ResponseEntity doLoadBookings(LoadBookingsRequest loadBookingsRequest,
                                          BankApiTO bankApi, ScaStatus scaStatus) {
        log.debug("process start > load booking list");
        BankAccessEntity bankAccessEntity = getBankAccessEntity(loadBookingsRequest);
        BankAccountEntity bankAccountEntity = getBankAccountEntity(loadBookingsRequest, bankAccessEntity);

        log.debug("load booking list from bank");
        List<BookingEntity> bookingEntities = bookingService.syncBookings(scaStatus, bankAccessEntity,
            bankAccountEntity, bankApiMapper.toBankApi(bankApi),
            credentialsMapper.toCredentials(loadBookingsRequest.getCredentials()));

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
        log.debug("save temporary user to db");
        //temporary user, will be deleted after x minutes
        UserEntity userEntity = createUser();
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = bankAccessMapper.toBankAccessEntity(bankAccess, userEntity.getId(), true);
        bankAccessEntity.setUserId(userEntity.getId());
        return bankAccessEntity;
    }

    private UserEntity createUser() {
        //temporary user, will be deleted after x minutes
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(thresholdTemporaryData));
        return userEntity;
    }

    private void selectScaMethodForConsent(String consentId, String scaMethodId) {
        log.debug("set selected 2FA method to consent");
        SelectPsuAuthenticationMethodRequestTO selectPsuAuthenticationMethodRequest =
            new SelectPsuAuthenticationMethodRequestTO();
        selectPsuAuthenticationMethodRequest.setAuthenticationMethodId(scaMethodId);
        consentService.selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest, consentId);
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

    private ResponseEntity<Resource<UpdateAuthResponseTO>> createChallengeResponse(UpdateAuthResponse response,
                                                                                   String consentId,
                                                                                   String authorisationId) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(consentId,
            authorisationId)).withSelfRel());
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).transactionAuthorisation(consentId,
            authorisationId, null)).withRel("transactionAuthorisation"));
        response.setScaStatus(SCAMETHODSELECTED);
        return ResponseEntity.ok(new Resource<>(consentAuthorisationMapper.toUpdateAuthResponseTO(response), links));
    }

    @Data
    public static class LoadAccountsRequest {
        @NotNull BankAccessTO bankAccessTO;
        CredentialsTO credentials;
        String scaMethodId;
    }

    @Data
    public static class LoadBankAccountsResponse {
        List<BankAccountTO> bankAccounts;
    }

    @Data
    public static class LoadBookingsRequest {
        String userId;
        String accessId;
        String accountId;
        BankAccessTO bankAccess;
        CredentialsTO credentials;
        String scaMethodId;
    }

    @Data
    public static class LoadBookingsResponse {
        List<BookingTO> bookings;
        BalancesReportTO balances;

    }
}
