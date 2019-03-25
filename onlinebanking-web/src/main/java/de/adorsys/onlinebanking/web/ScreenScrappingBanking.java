package de.adorsys.onlinebanking.web;

import spi.OnlineBankingService;

import java.util.List;

/**
 * Created by alexg on 13.12.17.
 */
public class ScreenScrappingBanking implements OnlineBankingService {

    @Override
    public BankApi bankApi() {
        return BankApi.SCREEN_SCRAPPING;
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
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode,
                                              String pin, boolean storePin) {
        return null;
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {

    }

    @Override
    public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode,
                                             BankAccount bankAccount, String pin) {
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
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode,
                              BankAccount bankAccount, String pin, Payment payment) {

    }

    @Override
    public void submitPayment(Payment payment, String tan) {

    }
}
