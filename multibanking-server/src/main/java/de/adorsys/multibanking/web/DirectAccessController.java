package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ExternalAuthorisationRequiredException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.mapper.*;
import de.adorsys.multibanking.web.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@UserResource
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/direct")
public class DirectAccessController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final BookingMapper bookingMapper;
    private final BankApiMapper bankApiMapper;
    private final BalancesMapper balancesMapper;
    private final ConsentMapper consentMapper;
    private final BankAccessMapper bankAccessMapper;
    private final BankAccountMapper bankAccountMapper;
    private final BankAccountService bankAccountService;
    private final BookingService bookingService;
    private final UserRepositoryIf userRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;

    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @ApiOperation(value = "Read bank accounts")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBankAccountsResponse.class),
        @ApiResponse(code = 202, message = "Consent authorisation required", response = Consent.class)})
    @PutMapping("/accounts")
    public ResponseEntity<LoadBankAccountsResponse> loadBankAccounts(@RequestBody BankAccessTO bankAccess,
                                                                     @RequestParam(required = false) BankApiTO bankApi) {
        //temporary user, will be deleted after x minutes
        logger.debug("process start > load bank account list");
        logger.debug("save temporary user to db");
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(thresholdTemporaryData));
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = bankAccessMapper.toBankAccessEntity(bankAccess);

        bankAccessEntity.setStorePin(false);
        bankAccessEntity.setUserId(userEntity.getId());

        LoadBankAccountsResponse response = new LoadBankAccountsResponse();

        try {
            logger.debug("load bank account list from bank");
            List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
                bankApiMapper.toBankApi(bankApi));


            logger.debug("save bank account list to db");
            bankAccessEntity.setPin(null);
            bankAccessEntity.setTemporary(true);
            bankAccessEntity.setUserId(userEntity.getId());
            bankAccessRepository.save(bankAccessEntity);

            bankAccounts.forEach(account -> {
                account.setBankAccessId(bankAccessEntity.getId());
                bankAccountRepository.save(account);
            });

            logger.debug("process finished < return bank account list");
            response.setBankAccounts(bankAccountMapper.toBankAccountTOs(bankAccounts));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ExternalAuthorisationRequiredException e) {
            response.setConsent(e.getConsent());
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }

    }

    @ApiResponses({
        @ApiResponse(code = 202, message = "Consent authorisation required", response = Consent.class)})
    @ApiOperation(value = "Read account bookings")
    @PutMapping("/bookings")
    public ResponseEntity<LoadBookingsResponse> loadBookings(@RequestBody LoadBookingsRequest loadBookingsRequest,
                                                             @RequestParam(required = false) BankApiTO bankApi) {
        logger.debug("process start > load booking list");
        logger.debug("load bank access");
        BankAccessEntity bankAccessEntity = bankAccessRepository.findOne(loadBookingsRequest.getAccessId())
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class,
                loadBookingsRequest.getAccessId()));

        logger.debug("load bank account");
        BankAccountEntity bankAccountEntity = bankAccountRepository.findOne(loadBookingsRequest.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class,
                loadBookingsRequest.getAccountId()));

        LoadBookingsResponse loadBookingsResponse = new LoadBookingsResponse();

        try {
            logger.debug("load booking list from bank");
            List<BookingEntity> bookings = bookingService.syncBookings(bankAccessEntity, bankAccountEntity,
                bankApiMapper.toBankApi(bankApi), loadBookingsRequest.getPin());
            loadBookingsResponse.setBookings(bookingMapper.toBookingTOs(bookings));
            loadBookingsResponse.setBalances(balancesMapper.toBalancesReportTO(bankAccountEntity.getBalances()));

            logger.debug("process finished < return booking list");
            return new ResponseEntity<>(loadBookingsResponse, HttpStatus.OK);
        } catch (ExternalAuthorisationRequiredException e) {
            loadBookingsResponse.setConsent(consentMapper.toConsentTO(e.getConsent()));
            return new ResponseEntity<>(loadBookingsResponse, HttpStatus.ACCEPTED);
        }
    }

    @Data
    public static class LoadBookingsRequest {
        String accessId;
        String accountId;
        String pin;
    }

    @Data
    public static class LoadBookingsResponse {
        ConsentTO consent;
        BalancesReportTO balances;
        List<BookingTO> bookings;
    }

    @Data
    public static class LoadBankAccountsResponse {
        Consent consent;
        List<BankAccountTO> bankAccounts;
    }

}
