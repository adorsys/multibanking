package de.adorsys.multibanking.web.userservice;

import de.adorsys.multibanking.banking.OnlineBankingService;
import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.repository.BankAccessRepository;
import de.adorsys.multibanking.repository.BankAccountRepository;
import de.adorsys.multibanking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@RestController
@RequestMapping(path = "api/v1/users/{userId}/bankaccesses/{accessId}/accounts")
public class BankAccountController {

    private static final Logger log = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private OnlineBankingService onlineBankingService;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private BankAccessRepository bankAccessRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @RequestMapping(method = RequestMethod.GET)
    public Resources<List<BankAccount>> getBankAccounts(@PathVariable("userId") String userId, @PathVariable(value = "accessId") String accessId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByBankAccessId(accessId).get();
        return new Resources(bankAccounts);
    }

    @RequestMapping(path = "/{accountId}/sync", method = RequestMethod.PUT)
    public Resources<List<Booking>> syncBookings(@PathVariable("userId") String userId, @PathVariable(value = "accessId") String accessId, @PathVariable(value = "accountId") String accountId, @RequestBody String pin) {
        BankAccess bankAccess = bankAccessRepository.findById(accessId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccess.class, accessId));

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccount.class, accountId));

        List<Booking> bookings = onlineBankingService.loadBookings(bankAccess, bankAccount, pin).get();
        bookings.forEach(booking -> booking.setAccountId(accountId));
        try {
            bookingRepository.insert(bookings);
        } catch (DuplicateKeyException e) {
            //ignore it
        }

        return new Resources(bookings);
    }
}
