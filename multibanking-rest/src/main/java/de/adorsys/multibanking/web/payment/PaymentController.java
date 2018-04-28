package de.adorsys.multibanking.web.payment;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.PaymentEntity;
import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.PaymentService;
import de.adorsys.multibanking.service.UserDataService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BaseController;
import domain.Payment;
import io.swagger.annotations.Api;
import lombok.Data;

/**
 * @author alexg on 07.02.17.
 * @author fpo 2018-03-23 03:46
 */
@UserResource
@RestController
@RequestMapping(path = PaymentController.BASE_PATH)
@Api(value = PaymentController.BASE_PATH, 
	tags = "MB-007 - Payments", description="Initiates and manages payments on behalves of the user.")
public class PaymentController extends BaseController {
	public static final String BASE_PATH = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/payments"; 

    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserDataService uds;

    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public Resource<PaymentEntity> getPayment(@PathVariable String accessId, @PathVariable String accountId, @PathVariable String paymentId) {
        PaymentEntity paymentEntity = paymentService.findPayment(accessId, accountId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentEntity.class, paymentId));
        return mapToResource(accessId, accountId, paymentEntity);
    }

    @RequestMapping(method = RequestMethod.POST)
    public HttpEntity<Void> createPayment(@PathVariable String accessId, @PathVariable String accountId, @RequestBody CreatePaymentRequest paymentRequest) {

    	UserData userData = uds.load();
    	BankAccessEntity bankAccessEntity = userData.bankAccessData(accessId).getBankAccess();
//    	BankAccessEntity bankAccessEntity= bankAccessService.loadbankAccess(accessId)
//    			.orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));
    	
    	BankAccountEntity bankAccountEntity = userData.bankAccountData(accessId, accountId).getBankAccount();
//    	BankAccountEntity bankAccountEntity = bankAccountService.loadBankAccount(accessId, accountId)
//				.orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        PaymentEntity payment = paymentService.createPayment(bankAccessEntity, bankAccountEntity, paymentRequest.getPin(), paymentRequest.getPayment());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(PaymentController.class).getPayment(accessId, accountId, payment.getId())).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{paymentId}/submit", method = RequestMethod.POST)
    public HttpEntity<Void> submitPayment(@PathVariable String accessId, @PathVariable String accountId, @PathVariable String paymentId, @RequestBody SubmitPaymentRequest paymentRequest) {
        
//    	BankAccessEntity bankAccessEntity= bankAccessService.loadbankAccess(accessId)
//    			.orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        if (!bankAccountService.exists(accessId, accountId)) {
            throw new ResourceNotFoundException(BankAccountEntity.class, accountId);
        }

        PaymentEntity paymentEntity = paymentService.findPayment(accessId, accountId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentEntity.class, paymentId));
        String bankCode = uds.load().bankAccessData(accessId).getBankAccess().getBankCode();
        paymentService.submitPayment(paymentEntity, bankCode, paymentRequest.getTan());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Resource<PaymentEntity> mapToResource(@PathVariable String accessId, @PathVariable String accountId, PaymentEntity paymentEntity) {
        return new Resource<>(paymentEntity,
                linkTo(methodOn(PaymentController.class).getPayment(accessId, accountId, paymentEntity.getId())).withSelfRel());
    }

    @Data
    private static class CreatePaymentRequest {
        Payment payment;
        String pin;
    }

    @Data
    private static class SubmitPaymentRequest {
        String tan;
    }
}
