package spi;


import domain.*;

import java.util.List;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String uid);

    void removeUser(BankApiUser bankApiUser);

    Bank getBankLoginSettings(String bankCode);

    List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

}