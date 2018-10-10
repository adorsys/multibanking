package hbci4java;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import domain.*;
import exception.InvalidPinException;
import hbci4java.job.*;
import hbci4java.model.HbciDialogFactory;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciTanSubmit;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import spi.OnlineBankingService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
public class Hbci4JavaBanking implements OnlineBankingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Hbci4JavaBanking() {
        this(null);
    }

    public Hbci4JavaBanking(InputStream customBankConfigInput) {
        if (customBankConfigInput != null) {
            try {
                HBCIUtils.refreshBLZList(customBankConfigInput);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (InputStream inputStream = HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream()) {
                HBCIUtils.refreshBLZList(inputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new Jdk8Module());
    }

    @Override
    public BankApi bankApi() {
        return BankApi.HBCI;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return false;
    }

    @Override
    public BankApiUser registerUser(Optional<String> bankingUrl, BankAccess bankAccess, String pin) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(Optional<String> bankingUrl, BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(Optional<String> bankingUrl, LoadAccountInformationRequest request) {
        try {
            checkBankExists(request.getBankCode(), bankingUrl);
            return AccountInformationJob.loadBankAccounts(request);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public Object createPayment(Optional<String> bankingUrl, BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, AbstractPayment payment) {
        try {
            checkBankExists(bankCode != null ? bankCode : bankAccess.getBankCode(), bankingUrl);
            return createPaymentJob(payment).createPayment(bankAccess, bankCode, pin, payment);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void submitPayment(Optional<String> bankingUrl, AbstractPayment payment, Object tanSubmit, String pin, String tan) {
        try {
            createPaymentJob(payment).submitPayment(payment, (HbciTanSubmit) tanSubmit, pin, tan);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void removeBankAccount(Optional<String> bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public LoadBookingsResponse loadBookings(Optional<String> bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        try {
            checkBankExists(loadBookingsRequest.getBankCode(), bankingUrl);
            return LoadBookingsJob.loadBookings(loadBookingsRequest);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    public HBCIDialog createDialog(Optional<String> bankingUrl, HbciDialogRequest dialogRequest) {
        try {
            checkBankExists(dialogRequest.getBankCode(), bankingUrl);
            return HbciDialogFactory.createDialog(dialogRequest);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        org.kapott.hbci.manager.BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        return bankInfo != null && bankInfo.getPinTanVersion() != null;
    }

    @Override
    public boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference) {
        return false;
    }

    @Override
    public void createAccountInformationConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest) {

    }

    private void checkBankExists(String bankCode, Optional<String> bankingUrl) {
        bankingUrl.ifPresent(s -> {
            BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
            if (bankInfo == null) {
                bankInfo = new BankInfo();
                bankInfo.setBlz(bankCode);
                bankInfo.setPinTanAddress(s);
                bankInfo.setPinTanVersion(HBCIVersion.HBCI_300);
                HBCIUtils.addBankInfo(bankInfo);
            }
        });
    }

    private AbstractPaymentJob createPaymentJob(AbstractPayment payment) {
        switch (payment.getPaymentType()) {
            case SINGLE_PAYMENT:
                return new SinglePaymentJob();
            case BULK_PAYMENT:
                return new BulkPaymentJob();
            case STANDING_ORDER:
                return new NewStandingOrderJob();
        }
        throw new IllegalArgumentException("invalid payment type " + payment.getPaymentType());
    }

    private RuntimeException handleHbciException(HBCI_Exception e) {
        Throwable processException = e;
        while (processException.getCause() != null && !(processException.getCause() instanceof InvalidPinException)) {
            processException = processException.getCause();
        }

        if (processException.getCause() != null && processException.getCause() instanceof InvalidPinException) {
            return (InvalidPinException) processException.getCause();
        }

        return e;
    }
}
