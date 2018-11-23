package de.adorsys.xs2a;

import de.adorsys.psd2.client.model.AccountDetails;
import de.adorsys.psd2.client.model.TransactionDetails;
import de.adorsys.psd2.client.model.TransactionsResponse200Json;
import domain.BankAccount;
import domain.BankApi;
import domain.Booking;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static domain.BankAccountType.fromXS2AType;

public class XS2AMapping {

    public static BankAccount toBankAccount(AccountDetails accountDetails) {
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

    public static List<Booking> toBookings(TransactionsResponse200Json transactionList) {
        return transactionList.getTransactions().getBooked()
                .stream()
                .map(XS2AMapping::toBooking)
                .collect(Collectors.toList());
    }

    private static Booking toBooking(TransactionDetails transactionDetails) {
        Booking booking = new Booking();
        booking.setBankApi(BankApi.XS2A);
        booking.setBookingDate(transactionDetails.getBookingDate());
        booking.setValutaDate(transactionDetails.getValueDate());
        booking.setAmount(new BigDecimal(transactionDetails.getTransactionAmount().getAmount()));
        booking.setCurrency(transactionDetails.getTransactionAmount().getCurrency());
        booking.setExternalId(transactionDetails.getEndToEndId());
        booking.setUsage(transactionDetails.getRemittanceInformationUnstructured());

        if (transactionDetails.getCreditorName() != null || transactionDetails.getDebtorName() != null) {
            BankAccount bankAccount = new BankAccount();
            bankAccount.setOwner(transactionDetails.getCreditorName() != null ? transactionDetails.getCreditorName(): transactionDetails.getDebtorName());
            booking.setOtherAccount(bankAccount);
        }

        return booking;
    }
}
