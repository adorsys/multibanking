package de.adorsys.multibanking.figo;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import de.adorsys.multibanking.domain.utils.Utils;
import me.figo.models.Account;
import me.figo.models.Challenge;
import me.figo.models.TanScheme;
import me.figo.models.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 13.09.17.
 */
public class FigoMapping {

    private static final Map<Frequency, String> FIGO_CYCLE = new HashMap<>();
    private static final Map<SinglePayment.TransactionType, String> FIGO_TRANSFER = new HashMap<>();

    static {
        FIGO_CYCLE.put(Frequency.WEEKLY, "weekly");
        FIGO_CYCLE.put(Frequency.MONTHLY, "monthly");
        FIGO_CYCLE.put(Frequency.TWO_MONTHLY, "two monthly");
        FIGO_CYCLE.put(Frequency.QUARTERLY, "quarterly");
        FIGO_CYCLE.put(Frequency.HALF_YEARLY, "half yearly");
        FIGO_CYCLE.put(Frequency.YEARLY, "yearly");

        FIGO_TRANSFER.put(SinglePayment.TransactionType.SINGLE_PAYMENT, "SEPA transfer");
        FIGO_TRANSFER.put(SinglePayment.TransactionType.STANDING_ORDER, "SEPA standing order");
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
            .balances(new BalancesReport()
                .readyBalance(Balance.builder().amount(account.getBalance().getBalance()).build()));
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
        figoPayment.setCurrency(payment.getCurrency());
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

    public static ChallengeData mapToChallenge(Challenge challenge) {
        ChallengeData challengeData = new ChallengeData();
        challengeData.setData(Collections.singletonList(challenge.getData()));
        challengeData.setOtpFormat(challenge.getFormat());
        challengeData.setAdditionalInformation(challenge.getTitle());
        return challengeData;
    }
}
