package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.transaction.*;

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

    TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadTransactionsRequest);

    StandingOrdersResponse loadStandingOrders(TransactionRequest<LoadStandingOrders> loadStandingOrdersRequest);

    LoadBalancesResponse loadBalances(TransactionRequest<LoadBalances> request);

    PaymentResponse executePayment(TransactionRequest<? extends AbstractPayment> paymentRequest);

    StrongCustomerAuthorisable getStrongCustomerAuthorisation();
}
