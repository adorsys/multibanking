package domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Created by alexg on 18.08.17.
 */
@Data
@ApiModel(description = "Standing order", value = "StandingOrder")
public class StandingOrder {

    public enum Cycle {
        TWO_WEEKLY,
        MONTHLY
    }

    private String orderId;
    private Cycle cycle;
    private int executionDay;
    private LocalDate firstBookingDate;
    private LocalDate lastBookingDate;
    private BigDecimal amount;
    private BankAccount otherAccount;
    private String usage;

    public boolean inCycle(LocalDate buchungsdatum) {
        boolean inInterval1 = inCycle(buchungsdatum.minusMonths(1), buchungsdatum);
        boolean inInterval2 = inCycle(buchungsdatum, buchungsdatum);
        boolean inInterval3 = inCycle(buchungsdatum.plusMonths(1), buchungsdatum);
        return inInterval1 || inInterval2 || inInterval3;
    }

    private boolean inCycle(LocalDate datum, LocalDate buchungsdatum) {
        LocalDate ausfuehrungsDatum = calcExecutionDate(datum);
        LocalDate startDatum = ausfuehrungsDatum.minusDays(7);
        LocalDate endDatum = ausfuehrungsDatum.plusDays(7);
        return startDatum.isBefore(buchungsdatum) && endDatum.isAfter(buchungsdatum);
    }

    private LocalDate calcExecutionDate(LocalDate localDate) {
        int jahr = localDate.getYear();
        int month = localDate.getMonthValue();
        int maxDays = YearMonth.from(localDate).lengthOfMonth();

        switch (executionDay) {
            case 99: // Ultimo
                return LocalDate.of(jahr, month, maxDays);
            case 98: // Ultimo -1
                return LocalDate.of(jahr, month, maxDays - 1);
            case 97: // Ultimo -2
                return LocalDate.of(jahr, month, maxDays - 2);
            default:
                return LocalDate.of(jahr, month, Math.min(executionDay, maxDays));
        }
    }

    public boolean usageContains(String bookingUsage) {
        return bookingUsage != null && usage != null
                && normalizeUsage(bookingUsage).contains(normalizeUsage(usage));
    }

    private static String normalizeUsage(String vwz) {
        return normalizeUsageUmlaute(vwz)
                .replaceAll("\\W", "").toLowerCase();
    }

    private static String normalizeUsageUmlaute(String vwz) {
        return vwz
                .replace("Ä", "Ae")
                .replace("ä", "ae")
                .replace("Ü", "Ue")
                .replace("ü", "ue")
                .replace("Ö", "Oe")
                .replace("ö", "oe")
                .replace("ß", "ss");
    }

}
