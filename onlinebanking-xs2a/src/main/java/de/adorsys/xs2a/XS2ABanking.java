package de.adorsys.xs2a;

import domain.*;
import spi.OnlineBankingService;

import java.util.List;

public class XS2ABanking implements OnlineBankingService {

    @Override
    public BankApi bankApi() {
        return BankApi.XS2A;
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
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {

    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin) {
        return null;
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {

    }

    @Override
    public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        return null;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        return false;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin, Payment payment) {

    }

    @Override
    public void submitPayment(Payment payment, String pin, String tan) {

    }
}
