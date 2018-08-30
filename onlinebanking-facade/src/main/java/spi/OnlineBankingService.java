package spi;


import domain.*;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String userId);

    void removeUser(BankApiUser bankApiUser);

    LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, Payment payment);

    void submitPayment(Payment payment, String pin, String tan);

    void createStandingOrder(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, StandingOrder standingOrder);

    void submitStandingOrder(StandingOrder standingOrder, String pin, String tan);
}
