package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.bg.model.*;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.request.CreateConsentRequest;
import org.iban4j.Iban;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.BankAccountType.fromXS2AType;

class BankingGatewayMapping {

    static BankAccount toBankAccount(AccountDetails accountDetails) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.bic(accountDetails.getBic());
        bankAccount.currency(accountDetails.getCurrency());
        bankAccount.iban(accountDetails.getIban());
        bankAccount.owner(accountDetails.getDetails());
        bankAccount.name(accountDetails.getName());
        bankAccount.type(fromXS2AType(accountDetails.getCashAccountType()));

        bankAccount.setExternalIdMap(new HashMap<>());
        bankAccount.getExternalIdMap().put(BankApi.XS2A, accountDetails.getResourceId());
        return bankAccount;
    }

    static BankAccount toBankAccount(AccountReference reference) {
        String iban = reference.getIban();
        BankAccount bankAccount = new BankAccount();
        bankAccount.setIban(iban);
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setBalances(new BalancesReport());
        return bankAccount;
    }

    static List<Booking> toBookings(TransactionsResponse200Json transactionList) {
        return transactionList.getTransactions().getBooked()
                .stream()
                .map(BankingGatewayMapping::toBooking)
                .collect(Collectors.toList());
    }

    private static Booking toBooking(TransactionDetails transactionDetails) {
        Booking booking = new Booking();
        booking.setBankApi(BankApi.BANKING_GATEWAY);
        booking.setBookingDate(transactionDetails.getBookingDate());
        booking.setValutaDate(transactionDetails.getValueDate());
        booking.setAmount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()));
        booking.setCurrency(transactionDetails.getTransactionAmount().getCurrency());
        booking.setExternalId(transactionDetails.getEndToEndId());
        booking.setUsage(transactionDetails.getRemittanceInformationUnstructured());

        if (transactionDetails.getCreditorName() != null || transactionDetails.getDebtorName() != null) {
            BankAccount bankAccount = new BankAccount();
            bankAccount.setOwner(transactionDetails.getCreditorName() != null ? transactionDetails.getCreditorName()
                    : transactionDetails.getDebtorName());
            booking.setOtherAccount(bankAccount);
        }

        return booking;
    }

    static Consents toConsents(CreateConsentRequest request) {
        Consents consents = new Consents();
        consents.setAccess(toAccountAccess(request));
        consents.setRecurringIndicator(request.isRecurringIndicator());
        consents.setValidUntil(request.getValidUntil());
        consents.setFrequencyPerDay(request.getFrequencyPerDay());
        consents.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        return consents;
    }

    private static AccountAccess toAccountAccess(CreateConsentRequest request) {
        AccountAccess accountAccess = new AccountAccess();

        if (request.isAvailableAccountsConsent()) {
            accountAccess.setAvailableAccounts(AccountAccess.AvailableAccountsEnum.ALLACCOUNTS);
        } else {
            accountAccess.setAccounts(toAccountReferences(request.getAccounts()));
            accountAccess.setBalances(toAccountReferences(request.getBalances()));
            accountAccess.setTransactions(toAccountReferences(request.getTransactions()));
        }

        return accountAccess;
    }

    private static List<AccountReference> toAccountReferences(List<BankAccount> accounts) {
        ArrayList<AccountReference> accountReferences = new ArrayList<>();
        for (BankAccount account : accounts) {
            AccountReference accountReference = new AccountReference();
            accountReference.setIban(account.getIban());
            accountReference.setCurrency(account.getCurrency());
            accountReferences.add(accountReference);
        }
        return accountReferences;
    }
}
