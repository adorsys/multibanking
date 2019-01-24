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

    PaymentResponse authenticatePsu(Optional<String> bankingUrl, AuthenticatePsuRequest authenticatePsuRequest);

    LoadAccountInformationResponse loadBankAccounts(Optional<String> bankingUrl,
                                                    LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(Optional<String> bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(Optional<String> bankingUrl, LoadBookingsRequest loadBookingsRequest);

    List<BankAccount> loadBalances(Optional<String> bankingUrl, LoadBalanceRequest loadBalanceRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    PaymentResponse initiatePayment(Optional<String> bankingUrl, PaymentRequest paymentRequest);

    Object requestPaymentAuthorizationCode(Optional<String> bankingUrl, PaymentRequest paymentRequest);

    String submitPayment(SubmitPaymentRequest submitPaymentRequest);

    Object deletePayment(Optional<String> bankingUrl, PaymentRequest paymentRequest);

    String submitDelete(SubmitPaymentRequest submitPaymentRequest);

    boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference);

    void createAccountInformationConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest);

}
