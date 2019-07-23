package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(BankAccess bankAccess, String pin);

    void removeUser(BankApiUser bankApiUser);

    LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                    LoadAccountInformationRequest loadAccountInformationRequest);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    AuthorisationCodeResponse requestAuthorizationCode(String bankingUrl, TransactionRequest paymentRequest);

    SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest);

    boolean psd2Scope();

    InitiatePaymentResponse initiatePayment(String bankingUrl, TransactionRequest paymentRequest);

    CreateConsentResponse createAccountInformationConsent(String bankingUrl, CreateConsentRequest createConsentRequest);

}
