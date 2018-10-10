package spi;


import domain.*;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String bankingUrl, BankAccess bankAccess, String pin);

    void removeUser(String bankingUrl, BankApiUser bankApiUser);

    LoadAccountInformationResponse loadBankAccounts(String bankingUrl, LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(String bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    Object createPayment(String bankingUrl, BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, AbstractPayment payment);

    void submitPayment(String bankingUrl, AbstractPayment payment, Object tanSubmit, String pin, String tan);
}
