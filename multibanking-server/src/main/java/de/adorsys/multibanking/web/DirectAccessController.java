package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import de.adorsys.multibanking.domain.spi.Consent;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final ConsentMapper consentMapper;
    private final BankAccessMapper bankAccessMapper;
    private final BankAccountMapper bankAccountMapper;
    private final BankAccountService bankAccountService;
    private final ConsentService consentService;
    private final BookingService bookingService;
    private final UserRepositoryIf userRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BookingRepositoryIf bookingRepository;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @ApiOperation(value = "Create consent")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Response", response = ConsentTO.class)})
    @PostMapping("/consents")
    public ResponseEntity<ConsentTO> createConsent(@Valid @RequestBody ConsentTO consent,
                                                   @RequestParam(required = false) BankApiTO bankApi) {
        log.debug("process start > create consent");
        Consent consentInput = consentMapper.toConsent(consent);
        Consent consentResponse = consentService.createConsent(consentInput, bankApiMapper.toBankApi(bankApi));

        log.debug("process finished < return consent");
        return new ResponseEntity<>(consentMapper.toConsentTO(consentResponse), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Progress consent to another status")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = ConsentTO.class)})
    @PutMapping("/consents")
    public ResponseEntity<ConsentTO> changeConsent(@Valid @RequestBody ConsentTO consent,
                                                   @RequestParam(required = false) BankApiTO bankApiTO) {
        log.debug("process start > change consent");
        Consent consentInput = consentMapper.toConsent(consent);
        BankApi bankApi = bankApiMapper.toBankApi(bankApiTO);
        consentInput = consentService.refreshConsent(consentInput, bankApi);
        ResponseEntity output;
        switch (consentInput.getScaStatus()) {
            case PSU_AUTHORISED:
                output = new ResponseEntity<>(
                    consentMapper.toConsentTO(
                        consentService.selectScaMethod(consentInput, bankApi)
                    ), HttpStatus.OK);
                break;
            case SCA_METHOD_SELECTED:
                output = new ResponseEntity<>(
                    consentMapper.toConsentTO(
                        consentService.authorizeConsent(consentInput, bankApi)
                    ), HttpStatus.OK);
                break;
            case PARTIALLY_AUTHORISED:
                // FIXME is this the status of the consent after creation with only a IBAN?
                output = new ResponseEntity<>(
                    consentMapper.toConsentTO(
                        consentService.loginConsent(consentInput, bankApi)
                    ), HttpStatus.OK);
                break;
            default:
                output = ResponseEntity.badRequest().build();
        }
        log.debug("process finished < return consent");
        return output;
    }

    @ApiOperation(value = "create challenge for loading bank accounts")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Response", response = LoadBankAccountsResponse.class)})
    @PostMapping("/accounts")
    public ResponseEntity<ChallengeTO> challengeBankAccounts(@Valid @RequestBody BankAccessTO bankAccess,
                                                                     @RequestParam(required = false) BankApiTO bankApiTO) {
        log.debug("process start > create challenge for bank account list");

        log.debug("load bank account list from bank");
        BankApi bankApi = bankApiMapper.toBankApi(bankApiTO);
        Consent consent = consentService.getConsent(bankAccess.getConsentId(), bankAccess.getIban(), bankApi);

        ChallengeTO response;
        if (bankApi == BankApi.HBCI) {
            BankAccessEntity accessEntity = prepareBankAccess(bankAccess);
            try {
                loadBankAccounts(bankAccess, bankApiTO);
                // in case HBCI allows the call we return an empty challenge
                return ResponseEntity.ok().build();
            } catch (MissingAuthorisationException e) {
                response = ChallengeMapper.map(e.getChallenge());
            }
        } else {
            consent = consentService.selectScaMethod(consent, bankApi);
            response = ChallengeMapper.map(consent.getChallenge());
        }

        log.debug("process finished < return bank account list");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
            bankApiMapper.toBankApi(bankApi));

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
    public ResponseEntity<LoadBookingsResponse> loadBookings(@Valid @RequestBody BankAccessTO bankAccess,
                                                             @RequestParam(required = false) BankApiTO bankApi) {
        log.debug("process start > load booking list");
        BankAccessEntity bankAccessEntity = prepareBankAccess(bankAccess);

        log.debug("create an account");
        BankAccountEntity bankAccountEntity = new BankAccountEntity();
        bankAccountEntity.setBankAccessId(bankAccessEntity.getId());
        bankAccountEntity.setIban(bankAccessEntity.getIban());
        bankAccountEntity.setUserId(bankAccessEntity.getUserId());
        bankAccountRepository.save(bankAccountEntity);

        log.debug("load bank account list from bank");
        List<BookingEntity> bookings = bookingService.syncBookings(bankAccessEntity, bankAccountEntity,
            bankApiMapper.toBankApi(bankApi));

        saveBankAccess(bankAccessEntity);

        log.debug("save bank account list to db");
        bookingRepository.save(bookings);

        LoadBookingsResponse response = new LoadBookingsResponse();
        response.setBookings(bookingMapper.toBookingTOs(bookings));
        log.debug("process finished < return booking list");
        return new ResponseEntity<>(response, HttpStatus.OK);
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
    public static class LoadBookingsResponse {
        ConsentTO consent;
        List<BookingTO> bookings;
    }

    @Data
    public static class LoadBankAccountsResponse {
        ConsentTO consent;
        List<BankAccountTO> bankAccounts;
    }

}
