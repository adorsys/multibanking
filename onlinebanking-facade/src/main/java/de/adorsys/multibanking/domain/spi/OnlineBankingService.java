package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.domain.transaction.AbstractPayment;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.domain.transaction.LoadTransactions;

public interface OnlineBankingService {

    BankApi bankApi();

    boolean externalBankAccountRequired();

    boolean userRegistrationRequired();

    BankApiUser registerUser(String userId);

    void removeUser(BankApiUser bankApiUser);

    void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser);

    boolean bankSupported(String bankCode);

    boolean bookingsCategorized();

    AccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> loadAccountInformationRequest);

    TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadBookingsRequest);

    AbstractResponse executePayment(TransactionRequest<AbstractPayment> paymentRequest);

    StrongCustomerAuthorisable getStrongCustomerAuthorisation();
}
