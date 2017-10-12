package figo;

import domain.*;
import me.figo.models.Account;
import me.figo.models.StandingOrder;
import me.figo.models.TanScheme;
import me.figo.models.Transaction;
import utils.Utils;

import java.time.ZoneId;

/**
 * Created by alexg on 13.09.17.
 */
public class FigoMapping {

    public static domain.StandingOrder mapStandingOrder(StandingOrder figoStandingOrder) {
        domain.StandingOrder standingOrder = new domain.StandingOrder();
        standingOrder.setOrderId(figoStandingOrder.getStandingOrderId());
        standingOrder.setExecutionDay(figoStandingOrder.getExecutionDay());
        standingOrder.setAmount(figoStandingOrder.getAmount());
        standingOrder.setUsage(figoStandingOrder.getPurposeText());
        standingOrder.setCycle(Cycle.valueOf(figoStandingOrder.getInterval().toString()));
        standingOrder.setOtherAccount(new BankAccount()
                .owner(figoStandingOrder.getName())
                .accountNumber(figoStandingOrder.getAccountNumber())
                .blz(figoStandingOrder.getBankCode())
                .currency(figoStandingOrder.getCurrency())
        );
        return standingOrder;
    }

    public static BankAccount mapBankAccount(Account account, BankApi bankApi) {
        return new BankAccount()
                .externalId(bankApi, account.getAccountId())
                .owner(account.getOwner())
                .accountNumber(account.getAccountNumber())
                .name(account.getName())
                .bankName(account.getBankName())
                .bic(account.getBIC())
                .blz(account.getBankCode())
                .iban(account.getIBAN())
                .type(BankAccountType.fromFigoType(account.getType()))
                .bankAccountBalance(new BankAccountBalance()
                        .readyHbciBalance(account.getBalance().getBalance()));
    }

    public static Booking mapBooking(Transaction transaction, BankApi bankApi) {
        Booking booking = new Booking();
        booking.setExternalId(transaction.getTransactionId());
        booking.setBankApi(bankApi);
        booking.setBookingDate(transaction.getBookingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        booking.setValutaDate(transaction.getValueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        booking.setAmount(transaction.getAmount());
        booking.setUsage(transaction.getPurposeText());
        booking.setText(transaction.getBookingText());
        booking.setTransactionCode(transaction.getTransactionCode());
        booking.setOtherAccount(mapBookingAccount(transaction));
        booking.setCreditorId(Utils.extractCreditorId(transaction.getPurposeText()));
        booking.setMandateReference(Utils.extractMandateReference(transaction.getPurposeText()));
        return booking;
    }

    public static BankAccount mapBookingAccount(Transaction transaction) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setName(transaction.getName());
        bankAccount.setBankName(transaction.getBankName());
        bankAccount.setCurrency(transaction.getCurrency());
        bankAccount.setAccountNumber(transaction.getAccountNumber());
        bankAccount.setBlz(transaction.getBankCode());
        return bankAccount;
    }

    public static TanTransportType mapTanTransportTypes(TanScheme tanScheme) {
        return TanTransportType.builder()
                .id(tanScheme.getTan_scheme_id())
                .name(tanScheme.getName())
                .medium(tanScheme.getMedium_name())
                .build();
    }
}
