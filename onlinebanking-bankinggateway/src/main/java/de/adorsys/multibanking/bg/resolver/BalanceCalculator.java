package de.adorsys.multibanking.bg.resolver;

import de.adorsys.multibanking.domain.Balance;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.Booking;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class BalanceCalculator {
    public void calculateBalance(List<Booking> bookings, List<Booking> pendingBookings, BalancesReport balancesReport) {
        boolean allBookingsHaveBalance = true;

        for (Booking booking : bookings) {
            if (booking.getBalance() == null) {
                allBookingsHaveBalance = false;
                break;
            }
        }

        if (allBookingsHaveBalance) {
            log.info("All bookings have balanceAfterTransaction. Skipping balance calculation");
            return;
        } else {
            log.info("Calculating balances");
        }

        Balance closingBookedBalance =  balancesReport.getReadyBalance();
        Balance expectedBalance = balancesReport.getUnreadyBalance();

        if (closingBookedBalance == null && expectedBalance != null && expectedBalance.getAmount() != null) {
            log.info("closingBookedBalance is null. Using expectedBalance for calculation");
            BigDecimal sum = pendingBookings.stream()
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            closingBookedBalance = Balance.builder()
                .amount(expectedBalance.getAmount().subtract(sum)) // expected = closingBooked + sum(pending)
                .build();
        }

        if (closingBookedBalance == null) {
            log.error("Cannot calculate balances. closingBookedBalance is null");
            return;
        }

        // calculate balance after transaction by subtracting the balance amounts from closing booked balance
        BigDecimal balance = closingBookedBalance.getAmount();
        for (Booking booking : bookings) {
            booking.setBalance(balance);
            booking.setExternalId(booking.getValutaDate() + "_" + booking.getAmount() + "_" + booking.getBalance()); // override fallback external id
            balance = balance.subtract(booking.getAmount());
        }
    }
}
