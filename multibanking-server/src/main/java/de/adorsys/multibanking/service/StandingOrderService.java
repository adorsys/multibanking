package de.adorsys.multibanking.service;

import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SubmitAuthorizationCodeRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.StandingOrder;
import de.adorsys.multibanking.exception.domain.MissingPinException;
import de.adorsys.multibanking.pers.spi.repository.StandingOrderRepositoryIf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Service
public class StandingOrderService {

    private final BankService bankService;
    private final UserService userService;
    private final StandingOrderRepositoryIf standingOrderRepository;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final FinTSProductConfig finTSProductConfig;

    Object createStandingOrder(BankAccessEntity bankAccess, Credentials credentials, StandingOrder standingOrder) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = userService.checkApiRegistration(bankingService,
            userService.findUser(bankAccess.getUserId()));

        if (credentials.getPin() == null) {
            throw new MissingPinException();
        }

        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        try {
            TransactionRequest request = new TransactionRequest();
            request.setBankApiUser(bankApiUser);
            request.setTransaction(standingOrder);
            request.setBankAccess(bankAccess);
            request.setCredentials(credentials);
            request.setBankCode(bankEntity.getBankApiBankCode());
            request.setBankUrl(bankEntity.getBankingUrl());
            request.setHbciProduct(finTSProductConfig.getProduct());
            Object tanSubmit = bankingService.requestPaymentAuthorizationCode(request);

            StandingOrderEntity target = new StandingOrderEntity();
            BeanUtils.copyProperties(standingOrder, target);
            target.setCreatedDateTime(new Date());
            target.setUserId(bankAccess.getUserId());
            target.setTanSubmitExternal(tanSubmit);

            standingOrderRepository.save(target);
            return tanSubmit;
        } catch (MultibankingException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }

    void submitStandingOrder(StandingOrder standingOrder, Object tanSubmit, BankAccessEntity bankAccess,
                             Credentials credentials, String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (credentials == null) {
            throw new MissingPinException();
        }

        try {
            SubmitAuthorizationCodeRequest request = new SubmitAuthorizationCodeRequest();
            request.setTransaction(standingOrder);
            request.setTanSubmit(tanSubmit);
            request.setTan(tan);

            request.setCredentials(credentials);
            request.setHbciProduct(finTSProductConfig.getProduct());
            bankingService.submitPaymentAuthorizationCode(request);
        } catch (MultibankingException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }
}
