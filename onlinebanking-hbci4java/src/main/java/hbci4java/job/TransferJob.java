package hbci4java.job;

import domain.AbstractScaTransaction;
import domain.Product;
import domain.SinglePayment;
import domain.request.TransactionRequest;
import hbci4java.model.HbciDialogRequest;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVUmbSEPA;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.util.Optional;

import static hbci4java.model.HbciDialogFactory.createDialog;

/**
 * Created by cbr on 28.02.19.
 */
@Slf4j
public class TransferJob {
    public void requestTransfer(TransactionRequest sepaTransactionRequest) {
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(sepaTransactionRequest.getBankCode() != null ? sepaTransactionRequest.getBankCode() :
                        sepaTransactionRequest.getBankAccess().getBankCode())
                .customerId(sepaTransactionRequest.getBankAccess().getBankLogin())
                .login(sepaTransactionRequest.getBankAccess().getBankLogin2())
                .hbciPassportState(sepaTransactionRequest.getBankAccess().getHbciPassportState())
                .pin(sepaTransactionRequest.getPin())
                .build();

        dialogRequest.setProduct(Optional.ofNullable(sepaTransactionRequest.getProduct())
                .map(product -> new Product(product.getName(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(sepaTransactionRequest.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        AbstractHBCIJob hbciJob = createHbciJob(sepaTransactionRequest.getTransaction(), dialog.getPassport(), null);

        dialog.addTask(hbciJob);

        // Let the Handler submitAuthorizationCode all jobs in one batch
        HBCIExecStatus dialogStatus = dialog.execute(true);
        if (!dialogStatus.isOK()) {
            log.warn(dialogStatus.getErrorString());
        }

        if (hbciJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new HBCI_Exception(hbciJob.getJobResult().getJobStatus().getErrorString());
        }
    }

    private AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport,
                                           String rawData) {
        SinglePayment singlePayment = (SinglePayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        AbstractSEPAGV sepagv = new GVUmbSEPA(passport, GVUmbSEPA.getLowlevelName(), rawData);

        sepagv.setParam("src", src);
        sepagv.setParam("dst", dst);
        sepagv.setParam("btg", new Value(singlePayment.getAmount()));
        sepagv.setParam("usage", singlePayment.getPurpose());

        sepagv.verifyConstraints();

        return sepagv;
    }

    Konto getDebtorAccount(AbstractScaTransaction sepaTransaction, PinTanPassport passport) {
        return Optional.ofNullable(sepaTransaction.getDebtorBankAccount())
                .map(bankAccount -> {
                    Konto konto = passport.findAccountByAccountNumber(bankAccount.getAccountNumber());
                    konto.iban = bankAccount.getIban();
                    konto.bic = bankAccount.getBic();
                    return konto;
                })
                .orElse(null);
    }
}
