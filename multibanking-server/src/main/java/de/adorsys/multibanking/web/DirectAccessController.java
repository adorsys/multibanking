package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.ConsentService;
import de.adorsys.multibanking.web.mapper.BalancesMapper;
import de.adorsys.multibanking.web.mapper.BankAccessMapper;
import de.adorsys.multibanking.web.mapper.BankAccountMapper;
import de.adorsys.multibanking.web.mapper.BankApiMapper;
import de.adorsys.multibanking.web.mapper.BookingMapper;
import de.adorsys.multibanking.web.mapper.ConsentAuthorisationMapper;
import de.adorsys.multibanking.web.model.BalancesReportTO;
import de.adorsys.multibanking.web.model.BankAccessTO;
import de.adorsys.multibanking.web.model.BankAccountTO;
import de.adorsys.multibanking.web.model.BankApiTO;
import de.adorsys.multibanking.web.model.BookingTO;
import de.adorsys.multibanking.web.model.ConsentTO;
import de.adorsys.multibanking.web.model.UpdateAuthResponseTO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final BankAccountService bankAccountService;
    private final BookingService bookingService;
    private final UserRepositoryIf userRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final ConsentService consentService;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @ApiOperation(value = "create challenge for accounts")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Response", response = LoadBankAccountsResponse.class)})
    @PostMapping("/accounts")
    public ResponseEntity<Resource<UpdateAuthResponseTO>> requestBankAccounts(@Valid @RequestBody BankAccessTO bankAccess,
                                                                           @RequestParam(required = false) BankApiTO bankApi) {
        log.debug("process start > load bank account list");
        BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);

        log.debug("set selected 2FA method to consent");
        SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethodRequest = new SelectPsuAuthenticationMethodRequest();
        selectPsuAuthenticationMethodRequest.setConsentId(bankAccess.getConsentId());
        selectPsuAuthenticationMethodRequest.setAuthorisationId(bankAccess.getAuthorisationId());
        selectPsuAuthenticationMethodRequest.setAuthenticationMethodId(bankAccess.getScaMethodId());
        consentService.selectPsuAuthenticationMethod(selectPsuAuthenticationMethodRequest);

        log.debug("load bank account list from bank");
        try {
            List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
                bankApiMapper.toBankApi(bankApi), ScaStatus.PSUAUTHENTICATED);
            log.debug("request for bank account list was without 2FA");
            log.debug("Process as usual but return empty challenge");

            saveBankAccess(bankAccessEntity);

            log.debug("save bank account list to db");
            bankAccounts.forEach(account -> {
                account.setBankAccessId(bankAccessEntity.getId());
                account.setUserId(bankAccessEntity.getUserId());
                bankAccountRepository.save(account);
            });

            log.debug("process finished < return empty challenge");
            return createChallengeResponse(new UpdateAuthResponse(), bankAccess.getConsentId(), bankAccess.getAuthorisationId());
        } catch (MissingConsentAuthorisationException e) {
            log.debug("process finished < return challenge");
            UpdateAuthResponse response = e.getResponse();
            String consentId = e.getConsentId();
            String authorisationId = e.getAuthorisationId();
            return createChallengeResponse(response, consentId, authorisationId);
        }
    }

    private ResponseEntity<Resource<UpdateAuthResponseTO>> createChallengeResponse(UpdateAuthResponse response, String consentId, String authorisationId) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).getConsentAuthorisationStatus(consentId,
            authorisationId)).withSelfRel());
        links.add(linkTo(methodOn(ConsentAuthorisationController.class).transactionAuthorisation(consentId,
            authorisationId, null)).withRel("transactionAuthorisation"));
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        return ResponseEntity.ok(new Resource<>(consentAuthorisationMapper.toUpdateAuthResponseTO(response), links));
    }

    @ApiOperation(value = "Read bank accounts")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBankAccountsResponse.class)})
    @PutMapping("/accounts")
    public ResponseEntity<LoadBankAccountsResponse> loadBankAccounts(@Valid @RequestBody BankAccessTO bankAccess,
                                                                     @RequestParam(required = false) BankApiTO bankApi) {
        log.debug("process start > load bank account list");
        BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);

        log.debug("load bank account list from bank");
        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            bankApiMapper.toBankApi(bankApi), ScaStatus.FINALISED);

        saveBankAccess(bankAccessEntity);

        log.debug("save bank account list to db");
        bankAccounts.forEach(account -> {
            account.setBankAccessId(bankAccessEntity.getId());
            account.setUserId(bankAccessEntity.getUserId());
            bankAccountRepository.save(account);
        });

        LoadBankAccountsResponse response = new LoadBankAccountsResponse();
        response.setBankAccounts(bankAccountMapper.toBankAccountTOs(bankAccounts));
        log.debug("process finished < return bank account list");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiResponses({
        @ApiResponse(code = 202, message = "Consent authorisation required", response = LoadBookingsResponse.class)})
    @ApiOperation(value = "Read account bookings")
    @PutMapping("/bookings")
    public ResponseEntity<LoadBookingsResponse> loadBookings(@Valid @RequestBody LoadBookingsRequest loadBookingsRequest,
                                                             @RequestParam(required = false) BankApiTO bankApi) {
        log.debug("process start > load booking list");
        log.debug("load bank access");
        BankAccessEntity bankAccessEntity = bankAccessRepository.findOne(loadBookingsRequest.getAccessId())
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class,
                loadBookingsRequest.getAccessId()));

        log.debug("load bank account");
        BankAccountEntity bankAccountEntity = bankAccountRepository.findOne(loadBookingsRequest.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class,
                loadBookingsRequest.getAccountId()));

        LoadBookingsResponse loadBookingsResponse = new LoadBookingsResponse();

        log.debug("load booking list from bank");
        List<BookingEntity> bookings = bookingService.syncBookings(bankAccessEntity, bankAccountEntity, bankApiMapper.toBankApi(bankApi));
        loadBookingsResponse.setBookings(bookingMapper.toBookingTOs(bookings));
        loadBookingsResponse.setBalances(balancesMapper.toBalancesReportTO(bankAccountEntity.getBalances()));

        log.debug("process finished < return booking list");
        return new ResponseEntity<>(loadBookingsResponse, HttpStatus.OK);
    }

    private BankAccessEntity prepareBankAccess(BankAccessTO bankAccess) {
        log.debug("save temporary user to db");
        //temporary user, will be deleted after x minutes
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(thresholdTemporaryData));
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = bankAccessMapper.toBankAccessEntity(bankAccess);
        bankAccessEntity.setStorePin(false);
        bankAccessEntity.setUserId(userEntity.getId());
        return bankAccessEntity;
    }

    private void saveBankAccess(BankAccessEntity bankAccessEntity) {
        log.debug("save bank access to db");
        bankAccessEntity.setPin(null);
        bankAccessEntity.setPin2(null);
        bankAccessEntity.setTemporary(true);
        bankAccessRepository.save(bankAccessEntity);
    }

    @Data
    public static class LoadBookingsRequest {
        @NotBlank
        String accessId;
        @NotBlank
        String accountId;
        @NotBlank
        String pin;
    }

    @Data
    public static class LoadBookingsResponse {
        ConsentTO consent;
        List<BookingTO> bookings;
        BalancesReportTO balances;
    }

    @Data
    public static class LoadBankAccountsResponse {
        ConsentTO consent;
        List<BankAccountTO> bankAccounts;
    }

}
