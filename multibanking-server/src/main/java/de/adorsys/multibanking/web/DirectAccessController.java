package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.bankinggateway.BankingGatewayAuthorisationService;
import de.adorsys.multibanking.web.mapper.*;
import de.adorsys.multibanking.web.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
    private final BankingGatewayAuthorisationService authorisationService;
    private final BookingService bookingService;
    private final UserRepositoryIf userRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

    @ApiOperation(value = "Create consent")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = ConsentTO.class)})
    @PutMapping("/consents")
    public ResponseEntity<ConsentTO> createConsent(@Valid @RequestBody ConsentTO consent,
                                                   @RequestParam(required = false) BankApiTO bankApi) {

        Consent consentResponse = authorisationService.createConsent(consent);

        return new ResponseEntity<>(consentMapper.toConsentTO(consentResponse), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Read bank accounts")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Response", response = LoadBankAccountsResponse.class),
        @ApiResponse(code = 202, message = "Consent authorisation required", response = Consent.class)})
    @PutMapping("/accounts")
    public ResponseEntity<LoadBankAccountsResponse> loadBankAccounts(@Valid @RequestBody BankAccessTO bankAccess,
                                                                     @RequestParam(required = false) BankApiTO bankApi) {
        //temporary user, will be deleted after x minutes
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(thresholdTemporaryData));
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = bankAccessMapper.toBankAccessEntity(bankAccess);

        bankAccessEntity.setStorePin(false);
        bankAccessEntity.setUserId(userEntity.getId());

        LoadBankAccountsResponse response = new LoadBankAccountsResponse();

        List<BankAccountEntity> bankAccounts = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            bankApiMapper.toBankApi(bankApi));

        bankAccessEntity.setPin(null);
        bankAccessEntity.setTemporary(true);
        bankAccessEntity.setUserId(userEntity.getId());
        bankAccessRepository.save(bankAccessEntity);

        bankAccounts.forEach(account -> {
            account.setBankAccessId(bankAccessEntity.getId());
            bankAccountRepository.save(account);
        });

        response.setBankAccounts(bankAccountMapper.toBankAccountTOs(bankAccounts));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiResponses({
        @ApiResponse(code = 202, message = "Consent authorisation required", response = Consent.class)})
    @ApiOperation(value = "Read account bookings")
    @PutMapping("/bookings")
    public ResponseEntity<LoadBookingsResponse> loadBookings(@RequestBody LoadBookingsRequest loadBookingsRequest,
                                                             @RequestParam(required = false) BankApiTO bankApi) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findOne(loadBookingsRequest.getAccessId())
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class,
                loadBookingsRequest.getAccessId()));

        BankAccountEntity bankAccountEntity = bankAccountRepository.findOne(loadBookingsRequest.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class,
                loadBookingsRequest.getAccountId()));

        LoadBookingsResponse loadBookingsResponse = new LoadBookingsResponse();

        List<BookingEntity> bookings = bookingService.syncBookings(bankAccessEntity, bankAccountEntity,
            bankApiMapper.toBankApi(bankApi), loadBookingsRequest.getPin());
        loadBookingsResponse.setBookings(bookingMapper.toBookingTOs(bookings));
        loadBookingsResponse.setBalances(balancesMapper.toBalancesReportTO(bankAccountEntity.getBalances()));

        return new ResponseEntity<>(loadBookingsResponse, HttpStatus.OK);
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
        ConsentTO consent;
        List<BankAccountTO> bankAccounts;
    }

}
