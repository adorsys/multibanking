package hbci4java;


import domain.BankAccount;
import domain.BankAccountBalance;
import domain.BankApi;
import domain.Booking;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.structures.Konto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexg on 08.02.17.
 */
public final class HbciFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HbciFactory.class);

    public static BankAccountBalance createBalance(GVRSaldoReq gvSaldoReq) {
        BankAccountBalance result = new BankAccountBalance();
        if (gvSaldoReq.isOK()) {
            GVRSaldoReq.Info[] infos = gvSaldoReq.getEntries();
            if (infos.length > 0) {
                if (infos[0] != null && infos[0].ready != null && infos[0].ready.value != null) {
                    result.setReadyHbciBalance(infos[0].ready.value.getBigDecimalValue().setScale(2));
                }
                if (infos[0] != null && infos[0].available != null) {
                    result.setAvailableHbciBalance(infos[0].available.getBigDecimalValue().setScale(2));
                }
                if (infos[0] != null && infos[0].kredit != null) {
                    result.setCreditHbciBalance(infos[0].kredit.getBigDecimalValue().setScale(2));
                }
                if (infos[0] != null && infos[0].unready != null && infos[0].unready.value != null) {
                    result.setUnreadyHbciBalance(infos[0].unready.value.getBigDecimalValue().setScale(2));
                }
                if (infos[0] != null && infos[0].used != null) {
                    result.setUsedHbciBalance(infos[0].used.getBigDecimalValue().setScale(2));
                }
            }
        }
        return result;
    }

    public static List<Booking> createBookings(GVRKUms gvrkUms) {
        List<Booking> bookings = new ArrayList<>();
        if (gvrkUms.isOK()) {
            List<GVRKUms.UmsLine> lines = gvrkUms.getFlatData();
            for (GVRKUms.UmsLine line : lines) {
                if (line == null) {
                    continue;
                }
                if (line.value == null) {
                    LOG.warn("Booking has no amount, skipping: %s", line);
                    continue;
                }
                if (line.bdate == null) {
                    LOG.warn("Booking has no booking date, skipping: %s", line);
                    continue;
                }
                Booking booking = new Booking();
                booking.setBankApi(BankApi.HBCI);
                booking.setBookingDate(line.bdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                booking.setAmount(line.value.getBigDecimalValue().setScale(2));
                booking.setAdditional(line.additional);
                booking.setAddkey(line.addkey);
                booking.setCustomerRef(line.customerref);
                booking.setInstRef(line.instref);
                booking.setReversal(line.isStorno);
                booking.setSepa(line.isSepa);
                booking.setPrimanota(line.primanota);
                booking.setText(line.text);
                booking.setValutaDate(line.valuta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                if (line.saldo != null && line.saldo.value != null) {
                    booking.setBalance(line.saldo.value.getBigDecimalValue().setScale(2));
                }
                if (line.charge_value != null) {
                    booking.setChargeValue(line.charge_value.getBigDecimalValue().setScale(2));
                }
                if (line.orig_value != null) {
                    booking.setOrigValue(line.orig_value.getBigDecimalValue().setScale(2));
                }
                if (line.other != null) {
                    booking.setOtherAccount(toBankAccount(line.other));
                }
                booking.setExternalId("B-" + line.bdate.getTime() + "_" + line.value.getLongValue()
                        + "_" + line.saldo.value.getLongValue());

                applyVerwendungszweck(line, booking);

                bookings.add(0, booking);
            }
        }
        LOG.debug("Received {} bookings: {}", bookings.size(), bookings);
        return bookings;
    }

    public static BankAccount toBankAccount(Konto konto) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.numberHbciAccount(konto.number);
        bankAccount.bicHbciAccount(konto.bic);
        bankAccount.blzHbciAccount(konto.blz);
        bankAccount.countryHbciAccount(konto.country);
        bankAccount.currencyHbciAccount(konto.curr);
        bankAccount.ibanHbciAccount(konto.iban);
        bankAccount.owner((konto.name + " " + (konto.name2 != null ? konto.name2 : "")).trim());
        bankAccount.typeHbciAccount(konto.type);
        return bankAccount;
    }

    private static void applyVerwendungszweck(GVRKUms.UmsLine u, Booking booking) {
        // BUGZILLA 146
        // Aus einer Mail von Stefan Palme
        //    Es geht noch besser. Wenn in "umsline.gvcode" nicht der Wert "999"
        //    drinsteht, sind die Variablen "text", "primanota", "usage", "other"
        //    und "addkey" irgendwie sinnvoll gef�llt.  Steht in "gvcode" der Wert
        //    "999" drin, dann sind diese Variablen alle null, und der ungeparste
        //    Inhalt des Feldes :86: steht komplett in "additional".

        String[] lines = u.usage.toArray(new String[u.usage.size()]);

        // die Bank liefert keine strukturierten Verwendungszwecke (gvcode=999).
        // Daher verwenden wir den gesamten "additional"-Block und zerlegen ihn
        // in 27-Zeichen lange Haeppchen
        if (lines.length == 0)
            lines = parse(u.additional);

        // Es gibt eine erste Bank, die 40 Zeichen lange Verwendungszwecke lieferte.
        // Siehe Mail von Frank vom 06.02.2014
        lines = rewrap(35, lines);

        String verwendungszweck = "";

        int lineIndex = 0;
        for (String line : lines) {
            int wordIndex = 0;
            String[] words = StringUtils.split(line, null, 0);
            for (String word : words) {
                verwendungszweck += word;
                verwendungszweck += wordIndex + 1 < words.length ? " " : "";
                wordIndex++;
            }
            verwendungszweck += lineIndex == 0 ? " " : "";
            lineIndex++;
        }

        booking.setUsage(WordUtils.capitalizeFully(verwendungszweck.trim(), ' ', '/'));
    }

    /**
     * Bereinigt die Verwendungszweck-Zeilen.
     * Hierbei werden leere Zeilen oder NULL-Elemente entfernt.
     * Ausserdem werden alle Zeilen getrimt.
     *
     * @param trim  wenn die Zeilen-Enden getrimmt werden sollen.
     * @param lines die zu bereinigenden Zeilen.
     * @return die bereinigten Zeilen.
     */
    private static List<String> clean(boolean trim, String... lines) {
        List<String> result = new ArrayList<>();
        if (lines == null || lines.length == 0)
            return result;

        for (String line : lines) {
            if (line == null)
                continue;

            if (trim)
                line = line.trim();
            if (line.length() > 0)
                result.add(line);
        }

        return result;
    }

    /**
     * Zerlegt einen langen Verwendungszweck in 27 Zeichen lange Haeppchen.
     *
     * @param line die zu parsende Zeile.
     * @return die 27 Zeichen langen Schnippsel.
     */
    private static String[] parse(String line) {
        if (line == null || line.length() == 0)
            return new String[0];

        // Java's Regex-Implementierung ist sowas von daemlich.
        // String.split() macht nur Rotz, wenn man mit Quantifierern
        // arbeitet. Also ersetzten wir erst mal alles gegen nen
        // eigenen String und verwenden den dann zum Splitten.
        String s = line.replaceAll("(.{27})", "$1--##--##");
        return s.split("--##--##");
    }

    /**
     * Bricht die Verwendungszweck-Zeilen auf $limit Zeichen lange Haeppchen neu um.
     * Jedoch nur, wenn wirklich Zeilen enthalten sind, die laenger sind.
     * Andernfalls wird nichts umgebrochen.
     *
     * @param limit das Zeichen-Limit pro Zeile.
     * @param lines die Zeilen.
     * @return die neu umgebrochenen Zeilen.
     */
    private static String[] rewrap(int limit, String... lines) {
        if (lines == null || lines.length == 0)
            return lines;

        boolean found = false;
        for (String s : lines) {
            if (s != null && s.length() > limit) {
                found = true;
                break;
            }
        }
        if (!found)
            return lines;

        List<String> l = clean(true, lines);

        // Zu einem String mergen
        StringBuilder sb = new StringBuilder();
        l.forEach(sb::append);
        String result = sb.toString();

        // und neu zerlegen
        String s = result.replaceAll("(.{" + limit + "})", "$1--##--##");
        return s.split("--##--##");
    }
}
