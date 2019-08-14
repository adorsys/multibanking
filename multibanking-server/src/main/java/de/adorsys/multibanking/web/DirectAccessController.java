package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationException;
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
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final BankAccountService bankAccountService;
    private final BookingService bookingService;
    private final UserRepositoryIf userRepository;
    private final ConsentService consentService;
    private final ConsentAuthorisationMapper consentAuthorisationMapper;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @ApiOperation(value = "create challenge for accounts")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Response", response = LoadBankAccountsResponse.class)})
    @PostMapping("/accounts")
    public ResponseEntity createHbciAccountsChallenge(@Valid @RequestBody BankAccessTO bankAccess,
                                                         @RequestParam(required = false) BankApiTO bankApi) {
        try {
            BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);
            selectScaMethodForConsent(bankAccess.getConsentId(), bankAccess.getScaMethodId());

            log.debug("load bank account list from bank");
            List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(SCAMETHODSELECTED,
                bankAccessEntity,
                bankApiMapper.toBankApi(bankApi));

            return createLoadBankAccountsResponse(bankAccounts);
        } catch (MissingConsentAuthorisationException e) {
            log.debug("process finished < return challenge");
            return createChallengeResponse(e.getResponse(), e.getConsentId(), e.getAuthorisationId());
        }
    }

    @ApiOperation(value = "Read bank accounts")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBankAccountsResponse.class)})
    @PutMapping("/accounts")
    public ResponseEntity<LoadBankAccountsResponse> loadBankAccounts(@Valid @RequestBody BankAccessTO bankAccess,
                                                                     @RequestParam(required = false) BankApiTO bankApi) {
        BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);

        log.debug("load bank account list from bank");
        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(FINALISED, bankAccessEntity,
            bankApiMapper.toBankApi(bankApi));

        log.debug("save bank account list to db");
        bankAccounts.forEach(account -> {
            account.setBankAccessId(bankAccessEntity.getId());
            account.setUserId(bankAccessEntity.getUserId());
        });

        return createLoadBankAccountsResponse(bankAccounts);
    }

    @ApiOperation(value = "create challenge for bookings")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Response", response = UpdateAuthResponseTO.class)})
    @PostMapping("/bookings")
    public ResponseEntity createHbciBookingsChallenge(@Valid @RequestBody BankAccessTO bankAccess,
                                                                                      @RequestParam(required = false) BankApiTO bankApi) {
        try {
            log.debug("process start > load booking list");
            BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);
            selectScaMethodForConsent(bankAccess.getConsentId(), bankAccess.getScaMethodId());

            log.debug("create bank account");
            BankAccountEntity bankAccountEntity = new BankAccountEntity();
            bankAccountEntity.setAccountNumber(Iban.valueOf(bankAccess.getIban()).getAccountNumber());
            bankAccountEntity.setBlz(Iban.valueOf(bankAccess.getIban()).getBankCode());
            bankAccountEntity.setIban(bankAccess.getIban());

            log.debug("load booking list from bank");
            List<BookingEntity> bookingEntities = bookingService.syncBookings(SCAMETHODSELECTED, bankAccessEntity,
                bankAccountEntity, bankApiMapper.toBankApi(bankApi));

            return createLoadBookingsResponse(bankAccountEntity, bookingEntities);
        } catch (MissingConsentAuthorisationException e) {
            log.debug("process finished < return challenge");
            return createChallengeResponse(e.getResponse(), e.getConsentId(), e.getAuthorisationId());
        }

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBookingsResponse.class)})
    @ApiOperation(value = "Read account bookings")
    @PutMapping("/bookings")
    public ResponseEntity<LoadBookingsResponse> loadBookings(@Valid @RequestBody BankAccessTO bankAccess,
                                                             @RequestParam(required = false) BankApiTO bankApi) {
        log.debug("process start > load booking list");
        BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);

        BankAccountEntity bankAccountEntity = new BankAccountEntity();
        bankAccountEntity.setBankAccessId(bankAccess.getId());
        bankAccountEntity.setUserId(bankAccessEntity.getUserId());
        bankAccountEntity.setIban(bankAccess.getIban());

        log.debug("load booking list from bank");
        List<BookingEntity> bookings = bookingService.syncBookings(FINALISED, bankAccessEntity, bankAccountEntity,
            bankApiMapper.toBankApi(bankApi));

        return createLoadBookingsResponse(bankAccountEntity, bookings);
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

    private ResponseEntity<LoadBookingsResponse> createLoadBookingsResponse(BankAccountEntity bankAccountEntity, List<BookingEntity> bookings) {
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
    public static class LoadBookingsResponse {
        List<BookingTO> bookings;
        BalancesReportTO balances;
    }

    @Data
    public static class LoadBankAccountsResponse {
        List<BankAccountTO> bankAccounts;
    }
}
