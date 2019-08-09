package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
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
import javax.validation.constraints.NotBlank;
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
    private final BankAccessMapper bankAccessMapper;
    private final BankAccountMapper bankAccountMapper;
    private final BankAccountService bankAccountService;
    private final BookingService bookingService;
    private final UserRepositoryIf userRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BookingRepositoryIf bookingRepository;
    @Value("${threshold_temporaryData:15}")
    private Integer thresholdTemporaryData;

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
