package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.response.SubmitAuthorizationCodeResponse;
import de.adorsys.multibanking.domain.transaction.*;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String userId);

    void removeUser(BankApiUser bankApiUser);

    LoadAccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> loadAccountInformationRequest);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    LoadBookingsResponse loadBookings(TransactionRequest<LoadBookings> loadBookingsRequest);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    AuthorisationCodeResponse initiatePayment(TransactionRequest<AbstractScaPaymentTransaction> paymentRequest);

    SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorisationCode submitAuthorisationCode);

    StrongCustomerAuthorisable getStrongCustomerAuthorisation();
}
