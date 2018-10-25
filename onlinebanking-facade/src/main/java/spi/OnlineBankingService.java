package spi;

import domain.*;
import domain.request.*;
import domain.response.LoadAccountInformationResponse;
import domain.response.LoadBookingsResponse;

import java.util.List;
import java.util.Optional;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(Optional<String> bankingUrl, BankAccess bankAccess, String pin);

    void removeUser(Optional<String> bankingUrl, BankApiUser bankApiUser);

    LoadAccountInformationResponse loadBankAccounts(Optional<String> bankingUrl,
                                                    LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(Optional<String> bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(Optional<String> bankingUrl, LoadBookingsRequest loadBookingsRequest);

    List<BankAccount> loadBalances(Optional<String> bankingUrl, LoadBalanceRequest loadBalanceRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    Object createPayment(Optional<String> bankingUrl, PaymentRequest paymentRequest);

    Object deletePayment(Optional<String> bankingUrl, PaymentRequest paymentRequest);

    String submitPayment(Optional<String> bankingUrl, SubmitPaymentRequest submitPaymentRequest);

    String submitDelete(Optional<String> bankingUrl, SubmitPaymentRequest submitPaymentRequest);

    boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference);

    void createAccountInformationConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest);
}
