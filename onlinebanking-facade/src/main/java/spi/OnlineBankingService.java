package spi;


import domain.*;

import java.io.IOException;
import java.util.List;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String uid, String bankCode);

    void removeUser(BankApiUser bankApiUser);

    List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, Payment payment);

    void submitPayment(Payment payment, String pin, String tan);

}
