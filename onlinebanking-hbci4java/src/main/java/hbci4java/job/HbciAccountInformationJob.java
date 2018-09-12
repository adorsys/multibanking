package hbci4java.job;

import domain.*;
import exception.HbciException;
import hbci4java.HbciDialogRequest;
import hbci4java.HbciMapping;
import hbci4java.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.*;
import java.util.stream.Collectors;

import static hbci4java.HbciDialogFactory.createDialog;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@Slf4j
public class HbciAccountInformationJob {

    public static LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest request) {
        log.info("Loading account list for bank [{}]", request.getBankCode());

        HBCIDialog dialog = createDialog(HbciDialogRequest.builder()
                .bankCode(request.getBankCode() != null ? request.getBankCode() : request.getBankAccess().getBankCode())
                .customerId(request.getBankAccess().getBankLogin())
                .login(request.getBankAccess().getBankLogin2())
                .hbciPassportState(request.getBankAccess().getHbciPassportState())
                .pin(request.getPin())
                .build());

        if (!dialog.getPassport().jobSupported("SEPAInfo"))
            throw new RuntimeException("SEPAInfo job not supported");

        log.info("fetching SEPA information");
        dialog.addTask(newJob("SEPAInfo", dialog.getPassport()));

        // TAN-Medien abrufen
//        if (dialog.getPassport().jobSupported("TANMediaList")) {
//            log.info("fetching TAN media list");
//            dialog.addTask(newJob("TANMediaList", dialog.getPassport()));
//        }
        HBCIExecStatus status = dialog.execute(true);

        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        request.getBankAccess().setBankName(dialog.getPassport().getInstName());
        List<BankAccount> hbciAccounts = new ArrayList<>();
        for (Konto konto : dialog.getPassport().getAccounts()) {
            BankAccount bankAccount = HbciMapping.toBankAccount(konto);
            bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
            bankAccount.bankName(request.getBankAccess().getBankName());
            hbciAccounts.add(bankAccount);
        }

//        if (request.isUpdateTanTransportTypes()) {
//            extractTanTransportTypes(dialog.getPassport()).ifPresent(tanTransportTypes -> {
//                if (request.getBankAccess().getTanTransportTypes() == null) {
//                    request.getBankAccess().setTanTransportTypes(new HashMap<>());
//                }
//                request.getBankAccess().getTanTransportTypes().put(BankApi.HBCI, tanTransportTypes);
//            });
//        }

        request.getBankAccess().setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        return LoadAccountInformationResponse.builder()
                .bankAccounts(hbciAccounts)
                .build();
    }

    public static Optional<List<TanTransportType>> extractTanTransportTypes(PinTanPassport hbciPassport) {
        if (hbciPassport.getUPD() != null) {
            return Optional.of(
                    hbciPassport.getUserTwostepMechanisms()
                            .stream()
                            .map(id -> hbciPassport.getBankTwostepMechanisms().get(id))
                            .filter(Objects::nonNull)
                            .map(hbciTwoStepMechanism -> TanTransportType.builder()
                                    .id(hbciTwoStepMechanism.getSecfunc())
                                    .name(hbciTwoStepMechanism.getName())
                                    .inputInfo(hbciTwoStepMechanism.getInputinfo())
                                    .medium(hbciPassport.getTanMedia(hbciTwoStepMechanism.getId()) != null ? hbciPassport.getTanMedia(hbciTwoStepMechanism.getId()).mediaName : null)
                                    .build())
                            .collect(Collectors.toList())
            );
        } else {
            log.warn("missing passport upd, unable find transport types for bank code {}", hbciPassport.getBLZ());
            return Optional.empty();
        }
    }
}
