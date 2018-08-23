package hbci4java.job;

import domain.BankAccess;
import domain.BankAccount;
import domain.BankApi;
import domain.TanTransportType;
import exception.HbciException;
import hbci4java.HbciMapping;
import hbci4java.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static hbci4java.HbciDialogFactory.createDialog;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@Slf4j
public class HbciAccountInformationJob {

    public static List<BankAccount> loadBankAccounts(BankAccess bankAccess, String bankCode, String pin) {
        log.info("Loading account list for bank [{}]", bankAccess.getBankCode());

        HBCIDialog dialog = createDialog(bankAccess, bankCode, null, pin);

        if (!dialog.getPassport().jobSupported("SEPAInfo"))
            throw new RuntimeException("SEPAInfo job not supported");

        log.info("fetching SEPA information");
        dialog.addTask(newJob("SEPAInfo", dialog.getPassport()));

        // TAN-Medien abrufen
        if (dialog.getPassport().jobSupported("TANMediaList")) {
            log.info("fetching TAN media list");
            dialog.addTask(newJob("TANMediaList", dialog.getPassport()));
        }
        HBCIExecStatus status = dialog.execute(true);

        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        bankAccess.setBankName(dialog.getPassport().getInstName());
        List<BankAccount> hbciAccounts = new ArrayList<>();
        for (Konto konto : dialog.getPassport().getAccounts()) {
            BankAccount bankAccount = HbciMapping.toBankAccount(konto);
            bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
            bankAccount.bankName(bankAccess.getBankName());
            hbciAccounts.add(bankAccount);
        }

        updateTanTransportTypes(bankAccess, dialog.getPassport());

        bankAccess.setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        return hbciAccounts;
    }

    public static void updateTanTransportTypes(BankAccess bankAccess, PinTanPassport hbciPassport) {
        if (bankAccess.getTanTransportTypes() == null) {
            bankAccess.setTanTransportTypes(new HashMap<>());
        }
        bankAccess.getTanTransportTypes().put(BankApi.HBCI, new ArrayList<>());

        if (hbciPassport.getUPD() != null) {
            hbciPassport.getUserTwostepMechanisms().forEach(id -> {
                HBCITwoStepMechanism properties = hbciPassport.getBankTwostepMechanisms().get(id);

                if (properties != null) {
                    String name = properties.getName();
                    bankAccess.getTanTransportTypes().get(BankApi.HBCI).add(
                            TanTransportType.builder()
                                    .id(id)
                                    .name(name)
                                    .inputInfo(properties.getInputinfo())
                                    .medium(hbciPassport.getTanMedia(name) != null ? hbciPassport.getTanMedia(name).mediaName : null)
                                    .build()
                    );
                } else {
                    log.warn("unable find transport type {} for bank code {}", id, bankAccess.getBankCode());
                }
            });
        } else {
            log.warn("missing passport upd, unable find transport types or bank code {}", bankAccess.getBankCode());
        }
    }
}
