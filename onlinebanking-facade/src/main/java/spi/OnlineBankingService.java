package spi;


import domain.*;

import java.io.IOException;
import java.util.List;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String uid);

    BankLoginSettings getBankLoginSettings(String bankCode);

    List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin, boolean storePin);

    List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

}