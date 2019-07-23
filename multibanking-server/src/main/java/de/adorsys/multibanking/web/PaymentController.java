package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.PaymentException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.SinglePaymentRepositoryIf;
import de.adorsys.multibanking.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(tags = "Multibanking payment")
@RequiredArgsConstructor
@UserResource
@RestController
@RequestMapping(path = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/payments")
public class PaymentController {

    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final SinglePaymentRepositoryIf paymentRepository;
    private final PaymentService paymentService;
    private final Principal principal;

    @ApiOperation(
        value = "Read payment",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @GetMapping("/{paymentId}")
    public Resource<SinglePaymentEntity> getPayment(@PathVariable String accessId, @PathVariable String accountId,
                                              @PathVariable String paymentId) {
        SinglePaymentEntity paymentEntity = paymentRepository.findByUserIdAndId(principal.getName(), paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(SinglePaymentEntity.class, paymentId));

        return mapToResource(accessId, accountId, paymentEntity);
    }

    @ApiOperation(
        value = "Create new payment",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PostMapping
    public HttpEntity<Void> createPayment(@PathVariable String accessId, @PathVariable String accountId,
                                          @RequestBody CreatePaymentRequest paymentRequest) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(principal.getName(), accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));

        bankAccountRepository.findByUserIdAndId(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, accountId));

        String pin = paymentRequest.getPin() != null ? paymentRequest.getPin() : bankAccessEntity.getPin();
        if (pin == null) {
            throw new PaymentException("required pin not specified");
        }
        SinglePaymentEntity payment = paymentService.createSinglePayment(bankAccessEntity,
            paymentRequest.getTanTransportType(), paymentRequest.getPin(), paymentRequest.getPayment());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(PaymentController.class).getPayment(accessId, accountId, payment.getId())).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Submit payment",
        authorizations = {
            @Authorization(value = "multibanking_auth", scopes = {
                @AuthorizationScope(scope = "openid", description = "")
            })})
    @PostMapping("/{paymentId}/submit")
    public HttpEntity<Void> submitPayment(@PathVariable String accessId, @PathVariable String accountId,
                                          @PathVariable String paymentId,
                                          @RequestBody SubmitPaymentRequest paymentRequest) {
        BankAccessEntity bankAccessEntity = bankAccessRepository.findByUserIdAndId(principal.getName(), accessId)
            .orElseThrow(() -> new ResourceNotFoundException(BankAccessEntity.class, accessId));
        if (!bankAccountRepository.exists(accountId)) {
            throw new ResourceNotFoundException(BankAccountEntity.class, accountId);
        }

        SinglePaymentEntity paymentEntity = paymentRepository.findByUserIdAndId(principal.getName(), paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(SinglePaymentEntity.class, paymentId));

        paymentService.submitSinglePayment(paymentEntity, bankAccessEntity, paymentRequest.getPin(),
            paymentRequest.getTan());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Resource<SinglePaymentEntity> mapToResource(@PathVariable String accessId, @PathVariable String accountId,
                                                  SinglePaymentEntity paymentEntity) {
        return new Resource<>(paymentEntity,
            linkTo(methodOn(PaymentController.class).getPayment(accessId, accountId, paymentEntity.getId())).withSelfRel());
    }

    @Data
    private static class CreatePaymentRequest {
        TanTransportType tanTransportType;
        SinglePayment payment;
        String pin;
    }

    @Data
    private static class SubmitPaymentRequest {
        String pin;
        String tan;
    }
}
