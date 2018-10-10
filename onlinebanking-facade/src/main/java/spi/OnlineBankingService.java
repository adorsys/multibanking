package spi;


import domain.*;

import java.util.Optional;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(Optional<String> bankingUrl, BankAccess bankAccess, String pin);

    void removeUser(Optional<String> bankingUrl, BankApiUser bankApiUser);

    LoadAccountInformationResponse loadBankAccounts(Optional<String> bankingUrl, LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(Optional<String> bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(Optional<String> bankingUrl, LoadBookingsRequest loadBookingsRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    Object createPayment(Optional<String> bankingUrl, BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, AbstractPayment payment);

    void submitPayment(Optional<String> bankingUrl, AbstractPayment payment, Object tanSubmit, String pin, String tan);

    boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference);

    void createAccountInformationConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest);
}
