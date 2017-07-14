package spi;


import java.util.List;

import domain.BankAccess;
import domain.BankAccount;
import domain.BankApi;
import domain.BankApiUser;
import domain.BankInfos;
import domain.BankLoginSettings;
import domain.Booking;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String uid);

    void removeUser(BankApiUser bankApiUser);

    BankLoginSettings getBankLoginSettings(String bankCode);

    List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin, boolean storePin);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();
    
    List<BankInfos> getBankInfos(String query);

}