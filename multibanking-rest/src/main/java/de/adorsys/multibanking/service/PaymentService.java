package de.adorsys.multibanking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.SinglePaymentEntity;
import de.adorsys.multibanking.exception.MissingPinException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.base.ListUtils;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.FQNUtils;
import domain.BankApiUser;
import domain.PaymentRequest;
import domain.SinglePayment;
import domain.request.SubmitPaymentRequest;
import exception.HbciException;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spi.OnlineBankingService;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 20.10.17.
 * @author fpo 2018-03-23 03:53
 */
@Service
public class PaymentService {

    @Autowired
    private UserObjectService uos;
    @Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private UserDataService uds;
    @Autowired
    private BankService bankService;

    private static DocumentFQN paymentsFQN(SinglePaymentEntity target) {
        return FQNUtils.paymentsFQN(target.getBankAccessId(), target.getBankAccountId());
    }

    private static TypeReference<List<SinglePaymentEntity>> listType() {
        return new TypeReference<List<SinglePaymentEntity>>() {
        };
    }

    public SinglePaymentEntity createPayment(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin,
                                             SinglePayment payment) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = uds.checkApiRegistration(bankingService.bankApi(), bankAccess);

        pin = pin == null ? bankAccess.getPin() : pin;
        if (pin == null) {
            throw new MissingPinException();
        }

        BankEntity bankEntity = bankService.findByBankCode(bankAccess.getBankCode())
                .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode()));

        try {
            Object tanSubmit = bankingService.createPayment(Optional.ofNullable(bankEntity.getBankingUrl()),
                    PaymentRequest.builder()
                    .bankApiUser(bankApiUser)
                    .payment(payment)
                    .bankAccess(bankAccess)
                    .pin(pin)
                    .bankCode(bankEntity.getBlzHbci())
                    .build());

            SinglePaymentEntity pe = new SinglePaymentEntity();
            BeanUtils.copyProperties(payment, pe);

            pe.setUserId(bankAccess.getUserId());
            pe.setSenderAccountNumber(bankAccount.getAccountNumber());
            pe.setSenderIban(bankAccount.getIban());
            pe.setSenderBic(bankAccount.getBic());
            pe.setCreatedDateTime(new Date());
            pe.setBankAccessId(bankAccess.getId());
            pe.setBankAccountId(bankAccount.getId());
            pe.setTanSubmitExternal(tanSubmit);

            create(pe);
            return pe;

        } catch (HbciException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }
    }

    public void submitPayment(SinglePaymentEntity paymentEntity, String bankCode, String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankCode);

        SubmitPaymentRequest submitPaymentRequest = SubmitPaymentRequest.builder()
                .payment(paymentEntity)
                .tanSubmit(paymentEntity.getTanSubmitExternal())
                .tan(tan)
                .build();
        try {
            //TODO pin is needed here
            bankingService.submitPayment(submitPaymentRequest);
        } catch (HbciException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }

        delete(paymentEntity);
    }

    public Optional<SinglePaymentEntity> findPayment(String accessId, String accountId, String paymentId) {
        List<SinglePaymentEntity> persList =
                uos.load(FQNUtils.paymentsFQN(accessId, accountId), listType()).orElse(Collections.emptyList());
        return ListUtils.find(paymentId, persList);
    }

    private void create(SinglePaymentEntity payment) {
        List<SinglePaymentEntity> persList = uos.load(paymentsFQN(payment), listType()).orElse(Collections.emptyList());
        persList = ListUtils.updateList(Collections.singletonList(payment), persList);
        uos.store(paymentsFQN(payment), listType(), persList);
    }

    private boolean delete(SinglePaymentEntity payment) {
        List<SinglePaymentEntity> persList = uos.load(paymentsFQN(payment), listType()).orElse(Collections.emptyList());
        List<SinglePaymentEntity> newPersList = ListUtils.deleteList(Collections.singletonList(payment), persList);
        uos.store(paymentsFQN(payment), listType(), persList);
        return persList.size() - newPersList.size() != 0;
    }
}
