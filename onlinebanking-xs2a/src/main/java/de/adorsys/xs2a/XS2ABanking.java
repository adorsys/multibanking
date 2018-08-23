package de.adorsys.xs2a;

import de.adorsys.psd2.ApiClient;
import de.adorsys.psd2.ApiException;
import de.adorsys.psd2.api.AccountInformationServiceAisApi;
import de.adorsys.psd2.model.AccountAccess;
import de.adorsys.psd2.model.AccountReferenceIban;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.model.ConsentsResponse201;
import domain.*;
import spi.OnlineBankingService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class XS2ABanking implements OnlineBankingService {

    @Override
    public BankApi bankApi() {
        return BankApi.XS2A;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public BankApiUser registerUser(String uid, String bankCode) {
        AccountReferenceIban accountReferenceIban = new AccountReferenceIban()
                .iban(bankCode)
                .currency("EUR");

        Consents consents = new Consents()
                .access(new AccountAccess()
                        .addAccountsItem(accountReferenceIban)
                        .addBalancesItem(accountReferenceIban)
                        .addTransactionsItem(accountReferenceIban)
                )
                .frequencyPerDay(Integer.MAX_VALUE)
                .validUntil(LocalDate.now().plusYears(1000))
                .recurringIndicator(true);


        //TODO bankcode to remote url mapping needed
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8081");

        AccountInformationServiceAisApi service = new AccountInformationServiceAisApi(apiClient);
        try {
            ConsentsResponse201 consent = service.createConsent(UUID.randomUUID(), consents, null, null, null,
                    uid, null, null, null, false, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null,
                    null);

            BankApiUser bankApiUser = new BankApiUser();
            bankApiUser.setApiUserId(uid);
            bankApiUser.setBankApi(BankApi.XS2A);
            bankApiUser.setProperties(new HashMap<>());
            bankApiUser.getProperties().put("consentId", consent.getConsentId());

            return bankApiUser;
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {

    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin) {
        return null;
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {

    }

    @Override
    public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        return null;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        return true;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, Payment payment) {

    }

    @Override
    public void submitPayment(Payment payment, String pin, String tan) {

    }
}
