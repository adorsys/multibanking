package utils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Created by alexg on 18.05.17.
 */
public final class Utils {

    public static SecureRandom getSecureRandom() {
        try {
            SecureRandom random = new SecureRandom();
            byte seed[] = random.generateSeed(20);
            random.setSeed(seed);
            return random;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean inCycle(LocalDate buchungsdatum, int executionDay) {
        boolean inInterval1 = inCycle(buchungsdatum.minusMonths(1), buchungsdatum, executionDay);
        boolean inInterval2 = inCycle(buchungsdatum, buchungsdatum, executionDay);
        boolean inInterval3 = inCycle(buchungsdatum.plusMonths(1), buchungsdatum, executionDay);
        return inInterval1 || inInterval2 || inInterval3;
    }

    private static boolean inCycle(LocalDate datum, LocalDate buchungsdatum, int executionDay) {
        LocalDate ausfuehrungsDatum = calcExecutionDate(datum, executionDay);
        LocalDate startDatum = ausfuehrungsDatum.minusDays(7);
        LocalDate endDatum = ausfuehrungsDatum.plusDays(7);
        return startDatum.isBefore(buchungsdatum) && endDatum.isAfter(buchungsdatum);
    }

    private static LocalDate calcExecutionDate(LocalDate localDate, int executionDay) {
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

    public static boolean usageContains(String bookingUsage, String standingOrderUsage) {
        return bookingUsage != null && standingOrderUsage != null
                && normalizeUsage(bookingUsage).contains(normalizeUsage(standingOrderUsage));
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
