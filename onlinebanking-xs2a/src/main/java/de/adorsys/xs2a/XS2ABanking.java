package de.adorsys.xs2a;

import de.adorsys.psd2.ApiClient;
import de.adorsys.psd2.ApiException;
import de.adorsys.psd2.api.AccountInformationServiceAisApi;
import de.adorsys.psd2.model.*;
import domain.*;
import org.apache.commons.lang3.StringUtils;
import spi.OnlineBankingService;

import java.time.LocalDate;
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
        return false;
    }

    @Override
    public BankApiUser registerUser(String uid) {
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest loadAccountInformationRequest) {
        AccountReferenceIban accountReferenceIban = new AccountReferenceIban()
                .iban("DE81250400903312345678")
                .currency("EUR");

        Consents consents = new Consents()
                .access(new AccountAccess()
                        .availableAccounts(AccountAccess.AvailableAccountsEnum.ALLACCOUNTS)
                )
                .frequencyPerDay(Integer.MAX_VALUE)
                .validUntil(LocalDate.now().plusYears(1000))
                .recurringIndicator(true);

        UUID requestId = UUID.randomUUID();

        //TODO bankcode to remote url mapping needed
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8082");

        AccountInformationServiceAisApi service = new AccountInformationServiceAisApi(apiClient);
        try {
            ConsentsResponse201 consent = service.createConsent(requestId, consents, null, null, null,
                    loadAccountInformationRequest.getBankAccess().getBankLogin(), null, null, null,
                    false, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null, null);

            StartScaprocessResponse startScaprocessResponse = service.startConsentAuthorisation(consent.getConsentId(), requestId, null, null, null,
                    loadAccountInformationRequest.getBankAccess().getBankLogin(), null, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null);

            String authorisationLink = startScaprocessResponse.getLinks().get("startAuthorisationWithPsuAuthentication").toString();
            String authorizationId = StringUtils.substringAfterLast(authorisationLink, "/");

            UpdatePsuAuthentication updatePsuAuthentication = new UpdatePsuAuthentication();
            updatePsuAuthentication.psuData(new PsuData()
                    .password(loadAccountInformationRequest.getPin()));

            Object o = service.updateConsentsPsuData(consent.getConsentId(), authorizationId, requestId, updatePsuAuthentication, null, null,
                    null, loadAccountInformationRequest.getBankAccess().getBankLogin(), null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null);

            service.getAccountList(requestId, consent.getConsentId(), false, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null);

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {

    }

    @Override
    public LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
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
    public Object createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, Payment payment) {
        return null;
    }

    @Override
    public void submitPayment(Payment payment, Object tanSubmit, String pin, String tan) {

    }

    @Override
    public Object createStandingOrder(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, StandingOrder standingOrder) {
        return null;
    }

    @Override
    public void submitStandingOrder(StandingOrder standingOrder, Object tanSubmit, String pin, String tan) {

    }
}
