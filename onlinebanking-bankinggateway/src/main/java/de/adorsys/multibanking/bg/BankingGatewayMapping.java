package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.bg.model.AccountDetails;
import de.adorsys.multibanking.bg.model.TransactionDetails;
import de.adorsys.multibanking.bg.model.TransactionsResponse200Json;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.Booking;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.BankAccountType.fromXS2AType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
}
