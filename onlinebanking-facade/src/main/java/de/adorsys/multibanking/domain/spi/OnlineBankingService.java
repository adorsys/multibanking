package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.request.SubmitAuthorizationCodeRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.response.SubmitAuthorizationCodeResponse;

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

    AuthorisationCodeResponse requestPaymentAuthorizationCode(TransactionRequest paymentRequest);

    SubmitAuthorizationCodeResponse submitPaymentAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest);

    StrongCustomerAuthorisable getStrongCustomerAuthorisation();
}
