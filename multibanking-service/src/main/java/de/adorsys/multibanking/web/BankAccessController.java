package de.adorsys.multibanking.web;

import de.adorsys.multibanking.banking.OnlineBankingService;
import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.repository.BankAccessRepository;
import de.adorsys.multibanking.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by alexg on 07.02.17.
 */
@RestController
@RequestMapping(path = "api/v1/users/{userId}/bankaccesses")
public class BankAccessController {

    private static final Logger log = LoggerFactory.getLogger(BankAccessController.class);

    @Autowired
    BankAccessRepository bankAccessRepository;
    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    OnlineBankingService onlineBankingService;

    @RequestMapping(method = RequestMethod.GET)
    public Resources<List<BankAccess>> getBankAccesses(@PathVariable("userId") String userId) {

        List<BankAccess> bankAccesses = bankAccessRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccess.class, userId));

        return new Resources(bankAccesses);
    }

    @RequestMapping(value = "/{accessId}", method = RequestMethod.GET)
    public Resource<BankAccess> getBankAccess(@PathVariable("userId") String userId, @PathVariable(value = "accessId") String accessId) {

        BankAccess bankAccessEntity = bankAccessRepository.findById(accessId)
                .orElseThrow(() -> new ResourceNotFoundException(BankAccess.class, accessId));

        return new Resource<>(bankAccessEntity);
    }

    @RequestMapping(method = RequestMethod.POST)
    public HttpEntity<Void> createBankaccess(@PathVariable("userId") String userId, @RequestBody BankAccess bankAccess) {
        List<BankAccount> bankAccounts = onlineBankingService.loadBankAccounts(bankAccess, bankAccess.getPin())
                .orElseThrow(() -> new InvalidBankAccessException(bankAccess.getBankCode(), bankAccess.getBankLogin())
        );

        BankAccess persistedBankAccess = bankAccessRepository.save(bankAccess);
        log.info("Neuen Bankzugang [{}] angelegt.", persistedBankAccess);

        bankAccounts.forEach(account -> account.setBankAccessId(persistedBankAccess.getId()));
        bankAccountRepository.save(bankAccounts);
        log.info("[{}] Konten zu Bankzugang [{}] angelegt.", bankAccounts.size(), persistedBankAccess);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(BankAccessController.class).getBankAccess(userId, persistedBankAccess.getId())).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
}
