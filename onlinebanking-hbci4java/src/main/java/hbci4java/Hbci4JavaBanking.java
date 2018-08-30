package hbci4java;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import domain.*;
import exception.InvalidPinException;
import hbci4java.job.HbciAccountInformationJob;
import hbci4java.job.HbciLoadBookingsJob;
import hbci4java.job.HbciSinglePaymentJob;
import hbci4java.job.HbciNewStandingOrderJob;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIUtils;
import spi.OnlineBankingService;

import java.io.InputStream;

@Slf4j
public class Hbci4JavaBanking implements OnlineBankingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Hbci4JavaBanking() {
        try (InputStream inputStream = HBCIUtils.class.getClassLoader().getResource("blz.properties").openStream()) {
            HBCIUtils.refreshBLZList(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public BankApiUser registerUser(String uid) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest request) {
        try {
            return HbciAccountInformationJob.loadBankAccounts(request);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, Payment payment) {
        try {
            HbciSinglePaymentJob.createPayment(bankAccess, bankCode, pin, payment);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void submitPayment(Payment payment, String pin, String tan) {
        try {
            HbciSinglePaymentJob.submitPayment(payment, pin, tan);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void createStandingOrder(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, StandingOrder standingOrder) {
        try {
            HbciNewStandingOrderJob.createStandingOrder(bankAccess, bankCode, pin, standingOrder);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void submitStandingOrder(StandingOrder standingOrder, String pin, String tan) {
        try {
            HbciNewStandingOrderJob.submitStandingOrder(standingOrder, pin, tan);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
        try {
            return HbciLoadBookingsJob.loadBookings(loadBookingsRequest);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }


    @Override
    public boolean bankSupported(String bankCode) {
        org.kapott.hbci.manager.BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        return bankInfo != null && bankInfo.getPinTanVersion() != null;
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
