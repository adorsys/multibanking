package de.adorsys.multibanking.service;

import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SubmitAuthorizationCodeRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
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

    Object createStandingOrder(BankAccessEntity bankAccess, String pin, StandingOrder standingOrder) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = userService.checkApiRegistration(bankAccess, bankingService.bankApi());

        pin = pin == null ? bankAccess.getPin() : pin;
        if (pin == null) {
            throw new MissingPinException();
        }

        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        try {
            TransactionRequest request = TransactionRequest.builder()
                .bankApiUser(bankApiUser)
                .transaction(standingOrder)
                .bankAccess(bankAccess)
                .pin(pin)
                .bankCode(bankEntity.getBlzHbci())
                .build();
            request.setProduct(finTSProductConfig.getProduct());
            Object tanSubmit = bankingService.requestAuthorizationCode(bankEntity.getBankingUrl(),
                request);

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
                             String pin, String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        pin = pin == null ? bankAccess.getPin() : pin;
        if (pin == null) {
            throw new MissingPinException();
        }

        try {
            SubmitAuthorizationCodeRequest request = SubmitAuthorizationCodeRequest.builder()
                .sepaTransaction(standingOrder)
                .tanSubmit(tanSubmit)
                .pin(pin)
                .tan(tan)
                .build();
            request.setProduct(finTSProductConfig.getProduct());
            bankingService.submitAuthorizationCode(request);
        } catch (MultibankingException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }
}
