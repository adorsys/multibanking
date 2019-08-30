package de.adorsys.multibanking.service;

import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.BulkPayment;
import de.adorsys.multibanking.domain.transaction.RawSepaPayment;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import de.adorsys.multibanking.exception.domain.MissingPinException;
import de.adorsys.multibanking.pers.spi.repository.BulkPaymentRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.RawSepaTransactionRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.SinglePaymentRepositoryIf;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final UserService userService;
    private final BankService bankService;
    private final RawSepaTransactionRepositoryIf rawSepaTransactionRepository;
    private final SinglePaymentRepositoryIf singlePaymentRepository;
    private final BulkPaymentRepositoryIf bulkPaymentRepository;
    private final FinTSProductConfig finTSProductConfig;

    RawSepaTransactionEntity createSepaRawPayment(BankAccessEntity bankAccess, Credentials credentials,
                                                  RawSepaPayment payment) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = userService.checkApiRegistration(bankingService,
            userService.findUser(bankAccess.getUserId()));

        if (credentials.getPin() == null) {
            throw new MissingPinException();
        }

        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        try {
            TransactionRequest request = new TransactionRequest<>(payment, bankApiUser, bankAccess);
            request.setBank(bankEntity);
            request.setHbciProduct(finTSProductConfig.getProduct());
            Object tanSubmit = bankingService.initiatePayment(request);

            RawSepaTransactionEntity target = new RawSepaTransactionEntity();
            BeanUtils.copyProperties(payment, target);
            target.setUserId(bankAccess.getUserId());
            target.setCreatedDateTime(new Date());
            target.setTanSubmitExternal(tanSubmit);

            rawSepaTransactionRepository.save(target);
            return target;
        } catch (MultibankingException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }

    public SinglePaymentEntity createSinglePayment(BankAccessEntity bankAccess, Credentials credentials,
                                                   SinglePayment payment) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = userService.checkApiRegistration(bankingService,
            userService.findUser(bankAccess.getUserId()));

        if (credentials.getPin() == null) {
            throw new MissingPinException();
        }

        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        try {
            TransactionRequest request = new TransactionRequest<>(payment, bankApiUser, bankAccess);
            request.setBank(bankEntity);
            request.setHbciProduct(finTSProductConfig.getProduct());

            Object tanSubmit = bankingService.initiatePayment(request);

            SinglePaymentEntity target = new SinglePaymentEntity();
            BeanUtils.copyProperties(payment, target);
            target.setUserId(bankAccess.getUserId());
            target.setCreatedDateTime(new Date());
            target.setTanSubmitExternal(tanSubmit);

            singlePaymentRepository.save(target);
            return target;
        } catch (MultibankingException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }

    BulkPaymentEntity createBulkPayment(BankAccessEntity bankAccess, Credentials credentials, BulkPayment payment) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = userService.checkApiRegistration(bankingService,
            userService.findUser(bankAccess.getUserId()));

        if (credentials.getPin() == null) {
            throw new MissingPinException();
        }

        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        try {
            TransactionRequest request = new TransactionRequest<>(payment, bankApiUser, bankAccess);
            request.setBank(bankEntity);
            request.setHbciProduct(finTSProductConfig.getProduct());

            Object tanSubmit = bankingService.initiatePayment(request);

            BulkPaymentEntity target = new BulkPaymentEntity();
            BeanUtils.copyProperties(payment, target);
            target.setUserId(bankAccess.getUserId());
            target.setCreatedDateTime(new Date());
            target.setTanSubmitExternal(tanSubmit);

            bulkPaymentRepository.save(target);
            return target;
        } catch (MultibankingException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }

    void submitRawSepaTransaction(RawSepaTransactionEntity transactionEntity, BankAccessEntity bankAccess,
                                  Credentials credentials, String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (credentials == null) {
            throw new MissingPinException();
        }

//        try {
//            SubmitAuthorizationCodeRequest request = new SubmitAuthorizationCodeRequest();
//            request.setTransaction(transactionEntity);
//            request.setTanSubmit(transactionEntity.getTanSubmitExternal());
//            request.setTan(tan);
//
//            request.setCredentials(credentials);
//            request.setHbciProduct(finTSProductConfig.getProduct());
//            bankingService.submitAuthorizationCode(request);
//        } catch (MultibankingException e) {
//            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
//        }

        rawSepaTransactionRepository.delete(transactionEntity.getId());
    }

    public void submitSinglePayment(SinglePaymentEntity paymentEntity, BankAccessEntity bankAccess,
                                    Credentials credentials,
                                    String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (credentials == null) {
            throw new MissingPinException();
        }

//        try {
//            SubmitAuthorizationCodeRequest request = new SubmitAuthorizationCodeRequest();
//            request.setTransaction(paymentEntity);
//            request.setTanSubmit(paymentEntity.getTanSubmitExternal());
//            request.setTan(tan);
//
//            request.setCredentials(credentials);
//            request.setHbciProduct(finTSProductConfig.getProduct());
//            bankingService.submitAuthorizationCode(request);
//        } catch (MultibankingException e) {
//            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
//        }

        singlePaymentRepository.delete(paymentEntity.getId());
    }

    void submitBulkPayment(BulkPaymentEntity paymentEntity, BankAccessEntity bankAccess, Credentials credentials,
                           String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (credentials == null) {
            throw new MissingPinException();
        }

//        try {
//            SubmitAuthorizationCodeRequest request = new SubmitAuthorizationCodeRequest();
//            request.setTransaction(paymentEntity);
//            request.setTanSubmit(paymentEntity.getTanSubmitExternal());
//            request.setTan(tan);
//
//            request.setCredentials(credentials);
//            request.setHbciProduct(finTSProductConfig.getProduct());
//            bankingService.submitAuthorizationCode(request);
//        } catch (MultibankingException e) {
//            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
//        }

        bulkPaymentRepository.delete(paymentEntity.getId());
    }
}
