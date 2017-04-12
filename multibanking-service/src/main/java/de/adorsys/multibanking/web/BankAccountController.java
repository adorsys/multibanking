package de.adorsys.multibanking.web;

import de.adorsys.multibanking.banking.BankingService;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.repository.BankAccessRepository;
import de.adorsys.multibanking.repository.BankAccountRepository;
import de.adorsys.multibanking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/users/{userId}/bankaccesses/{accessId}/accounts")
public class BankAccountController {

    private static final Logger log = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private BankingService bankingService;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private BankAccessRepository bankAccessRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @RequestMapping(method = RequestMethod.GET)
    public Resources<List<BankAccountEntity>> getBankAccounts(@PathVariable("userId") String userId, @PathVariable(value = "accessId") String accessId) {
        List<BankAccountEntity> bankAccounts = bankAccountRepository.findByBankAccessId(accessId);
        return new Resources(bankAccounts);
    }

    @RequestMapping(value = "/{accountId}", method = RequestMethod.GET)
    public Resource<BankAccountEntity> getBankAccess(@PathVariable("userId") String userId, @PathVariable(value = "accountId") String accountId) {

        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        return new Resource<>(bankAccountEntity);
    }

    @RequestMapping(path = "/{accountId}/sync", method = RequestMethod.PUT)
    public HttpEntity<Void> syncBookings(@PathVariable("userId") String userId, @PathVariable(value = "accessId") String accessId, @PathVariable(value = "accountId") String accountId, @RequestBody String pin) {
        BankAccessEntity bankAccess = bankAccessRepository.findById(accessId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        BankAccountEntity bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        bankingService.loadBookings(bankAccess, bankAccount, pin);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
