package de.adorsys.xs2a;

import de.adorsys.psd2.model.AccountDetails;
import de.adorsys.psd2.model.TransactionDetails;
import de.adorsys.psd2.model.TransactionsResponse200Json;
import domain.BankAccount;
import domain.Booking;

import java.util.List;
import java.util.stream.Collectors;

import static domain.BankAccountType.fromXS2AType;

public class XS2AMapping {

    public static BankAccount toBankAccount(AccountDetails accountDetails) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.accountNumber(accountDetails.getResourceId());
        bankAccount.bic(accountDetails.getBic());
        bankAccount.currency(accountDetails.getCurrency());
        bankAccount.iban(accountDetails.getIban());
        bankAccount.owner(accountDetails.getDetails());
        bankAccount.name(accountDetails.getName());
        bankAccount.type(fromXS2AType(accountDetails.getCashAccountType()));
        return bankAccount;
    }

    public static List<Booking> toBookings(TransactionsResponse200Json transactionList) {
        return transactionList.getTransactions().getBooked()
                .stream()
                .map(XS2AMapping::toBooking)
                .collect(Collectors.toList());
    }

    private static Booking toBooking(TransactionDetails transactionDetails) {
        return null;
    }
}
