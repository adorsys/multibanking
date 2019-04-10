package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;

import java.util.List;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String bankingUrl, BankAccess bankAccess, String pin);

    void removeUser(String bankingUrl, BankApiUser bankApiUser);

    ScaMethodsResponse authenticatePsu(String bankingUrl, AuthenticatePsuRequest authenticatePsuRequest);

    LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                    LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(String bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest);

    List<BankAccount> loadBalances(String bankingUrl, LoadBalanceRequest loadBalanceRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    InitiatePaymentResponse initiatePayment(String bankingUrl, TransactionRequest paymentRequest);

    void executeTransactionWithoutSca(String bankingUrl, TransactionRequest paymentRequest);

    AuthorisationCodeResponse requestAuthorizationCode(String bankingUrl, TransactionRequest paymentRequest);

    SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest);

    boolean accountInformationConsentRequired();

    CreateConsentResponse createAccountInformationConsent(String bankingUrl, CreateConsentRequest createConsentRequest);

}
