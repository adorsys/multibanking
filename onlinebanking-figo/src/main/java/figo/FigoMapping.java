package figo;

import domain.*;
import me.figo.models.StandingOrder;
import me.figo.models.*;
import org.apache.commons.lang3.StringUtils;
import utils.Utils;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 13.09.17.
 */
public class FigoMapping {

    private static final Map<Cycle, String> FIGO_CYCLE = new HashMap<>();
    private static final Map<SinglePayment.TransactionType, String> FIGO_TRANSFER = new HashMap<>();

    static {
        FIGO_CYCLE.put(Cycle.WEEKLY, "weekly");
        FIGO_CYCLE.put(Cycle.MONTHLY, "monthly");
        FIGO_CYCLE.put(Cycle.TWO_MONTHLY, "two monthly");
        FIGO_CYCLE.put(Cycle.QUARTERLY, "quarterly");
        FIGO_CYCLE.put(Cycle.HALF_YEARLY, "half yearly");
        FIGO_CYCLE.put(Cycle.YEARLY, "yearly");

        FIGO_TRANSFER.put(SinglePayment.TransactionType.SINGLE_PAYMENT, "SEPA transfer");
        FIGO_TRANSFER.put(SinglePayment.TransactionType.STANDING_ORDER, "SEPA standing order");
    }

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
                .bankAccountBalance(new BalancesReport()
                        .readyHbciBalance(Balance.builder().amount(account.getBalance().getBalance()).build()));
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
        bankAccount.setIban(Utils.extractIban(transaction.getPurposeText()));
        return bankAccount;
    }

    public static TanTransportType mapTanTransportTypes(TanScheme tanScheme) {
        return TanTransportType.builder()
                .id(tanScheme.getTan_scheme_id())
                .name(tanScheme.getName())
//                .medium(tanScheme.getMedium_name())
                .build();
    }

    public static me.figo.models.Payment mapToFigoPayment(String accountId, SinglePayment payment) {
        me.figo.models.Payment figoPayment = new me.figo.models.Payment();
        figoPayment.setAccountId(accountId);

        if (!StringUtils.isEmpty(payment.getReceiverIban())) {
            figoPayment.setIban(payment.getReceiverIban());
        } else {
            figoPayment.setBankCode(payment.getReceiverBankCode());
            figoPayment.setAccountNumber(payment.getReceiverAccountNumber());
        }

        figoPayment.setAmount(payment.getAmount());
        figoPayment.setCurrency("EUR");
        figoPayment.setType(FIGO_TRANSFER.get(payment.getTransactionType()));
        figoPayment.setName(payment.getReceiver());
        figoPayment.setPurpose(payment.getPurpose());

        // Mögliche Dauerauftragsattribute
//        if (payment.getExecutionDay() > -1) {
//            figoPayment.setExecution_day(payment.getExecutionDay());
//        }
//
//        if (payment.getFirstExecutionDate() != null) {
//            figoPayment.setFirst_execution_date(Date.from(
//                    payment.getFirstExecutionDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//
//        if (payment.getLastExecutionDate() != null) {
//            figoPayment.setLast_execution_date(Date.from(
//                    payment.getLastExecutionDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
//        }
//
//        if (payment.getCycle() != null) {
//            figoPayment.setInterval(FIGO_CYCLE.get(payment.getCycle()));
//        }

        return figoPayment;
    }

    public static TanChallenge mapToChallenge(Challenge challenge) {
        return TanChallenge.builder()
                .data(challenge.getData())
                .format(challenge.getFormat())
                .label(challenge.getLabel())
                .title(challenge.getTitle())
                .build();
    }
}
