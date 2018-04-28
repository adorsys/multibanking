package de.adorsys.multibanking.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.PaymentEntity;
import de.adorsys.multibanking.exception.MissingPinException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.base.ListUtils;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.FQNUtils;
import domain.BankApiUser;
import domain.Payment;
import exception.PaymentException;
import spi.OnlineBankingService;

/**
 * @author alexg on 20.10.17.
 * @author fpo 2018-03-23 03:53
 * 
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

    public PaymentEntity createPayment(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin, Payment payment) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        BankApiUser bankApiUser = uds.checkApiRegistration(bankingService.bankApi(), bankAccess.getBankCode());

        pin = pin == null ? bankAccess.getPin() : pin;
        if (pin == null) {
            throw new MissingPinException();
        }

        String mappedBlz = bankService.findByBankCode(bankAccess.getBankCode())
                .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode())).getBlzHbci();

        try {
            bankingService.createPayment(bankApiUser, bankAccess, mappedBlz, bankAccount, pin, payment);
        } catch (PaymentException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }

        PaymentEntity pe = new PaymentEntity();
        BeanUtils.copyProperties(payment, pe);
        pe.setUserId(bankAccess.getUserId());
        pe.setCreatedDateTime(new Date());
        pe.setBankAccessId(bankAccess.getId());
        pe.setBankAccountId(bankAccount.getId());

        create(pe);
        return pe;
    }

	public void submitPayment(PaymentEntity paymentEntity, String bankCode, String tan) {
        OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankCode);

        try {
            bankingService.submitPayment(paymentEntity, tan);
        } catch (PaymentException e) {
            throw new de.adorsys.multibanking.exception.PaymentException(e.getMessage());
        }

        delete(paymentEntity);
    }
	
	public Optional<PaymentEntity> findPayment(String accessId, String accountId, String paymentId){
		List<PaymentEntity> persList = uos.load(FQNUtils.paymentsFQN(accessId, accountId), listType()).orElse(Collections.emptyList());
		return ListUtils.find(paymentId, persList);
	}

    private void create(PaymentEntity payment) {
		List<PaymentEntity> persList = uos.load(paymentsFQN(payment), listType()).orElse(Collections.emptyList());
		persList = ListUtils.updateList(Collections.singletonList(payment), persList);
		uos.store(paymentsFQN(payment), listType(), persList);
	}
	
	private boolean delete(PaymentEntity payment) {
		List<PaymentEntity> persList = uos.load(paymentsFQN(payment), listType()).orElse(Collections.emptyList());
		List<PaymentEntity> newPersList = ListUtils.deleteList(Collections.singletonList(payment), persList);
		uos.store(paymentsFQN(payment), listType(), persList);
		return persList.size() - newPersList.size()!=0;
	}

    private static DocumentFQN paymentsFQN(PaymentEntity target){
    	return FQNUtils.paymentsFQN(target.getBankAccessId(), target.getBankAccountId());
    }

	private static TypeReference<List<PaymentEntity>> listType(){
		return new TypeReference<List<PaymentEntity>>() {};
	}
}
