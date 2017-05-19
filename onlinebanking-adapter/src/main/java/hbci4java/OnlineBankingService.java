package hbci4java;


import domain.*;

import java.util.List;

public interface OnlineBankingService {

    BankApi bankApiIdentifier();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String uid);

    List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin);

    List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

}