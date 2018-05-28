package hbci4java;


import domain.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.structures.Konto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.Utils.extractIban;

/**
 * Created by alexg on 08.02.17.
 */
public final class HbciMapping {

    private static final Logger LOG = LoggerFactory.getLogger(HbciMapping.class);

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

    public static List<StandingOrder> createStandingOrders(GVRDauerList gvrDauerList) {
        GVRDauerList.Dauer[] lines = gvrDauerList.getEntries();
        List<StandingOrder> standingOrders = new ArrayList<>();

        for (int i = 0; i < lines.length; ++i) {
            GVRDauerList.Dauer line = lines[i];
            StandingOrder auftrag = new StandingOrder();

            if (line.firstdate != null) {
                auftrag.setFirstExecutionDate(
                        line.firstdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            if (line.lastdate != null) {
                auftrag.setLastExecutionDate(
                        line.lastdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            auftrag.setAmount(line.value.getBigDecimalValue());
            auftrag.setOrderId(line.orderid);
            auftrag.setOtherAccount(toBankAccount(line.other));
            auftrag.setUsage(getUsage(Arrays.asList(line.usage)));
            auftrag.setExecutionDay(line.execday);

            Cycle cycle = null;
            if (!StringUtils.endsWithIgnoreCase("M", line.timeunit)) {
                cycle = Cycle.WEEKLY;
            } else {
                switch (line.turnus) {
                    case 1:
                        cycle = Cycle.MONTHLY;
                        break;
                    case 2:
                        cycle = Cycle.TWO_MONTHLY;
                        break;
                    case 3:
                        cycle = Cycle.QUARTERLY;
                        break;
                    case 6:
                        cycle = Cycle.HALF_YEARLY;
                        break;
                    case 12:
                        cycle = Cycle.YEARLY;
                        break;
                }
            }
            auftrag.setCycle(cycle);

            standingOrders.add(auftrag);
        }
        return standingOrders;

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

                // die Bank liefert keine strukturierten Verwendungszwecke (gvcode=999).
                // Daher verwenden wir den gesamten "additional"-Block und zerlegen ihn
                // in 27-Zeichen lange Haeppchen
                booking.setUsage(
                        getUsage(line.usage.size() > 0 ? line.usage : splitEqually(line.additional, 27)));

                String differentInitiator = Utils.extractDifferentInitiator(booking.getUsage());
                if (differentInitiator != null){
                    booking.getOtherAccount().setOwner(booking.getOtherAccount().getOwner()+" "+differentInitiator);
                }

                if (StringUtils.isBlank(booking.getOtherAccount().getIban())) {
                    booking.getOtherAccount().setIban(extractIban(booking.getUsage()));
                }

                bookings.add(0, booking);
            }
        }
        LOG.debug("Received {} bookings: {}", bookings.size(), bookings);
        return bookings;
    }

    public static BankAccount toBankAccount(Konto konto) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.accountNumber(konto.number);
        bankAccount.bic(konto.bic);
        bankAccount.blz(konto.blz);
        bankAccount.country(konto.country);
        bankAccount.currency(konto.curr);
        bankAccount.iban(konto.iban);
        bankAccount.owner((konto.name + (konto.name2 != null ? konto.name2 : "")).trim());
        bankAccount.name(konto.type);
        bankAccount.type(BankAccountType.fromHbciType(NumberUtils.toInt(konto.acctype)));
        return bankAccount;
    }

    private static String getUsage(List<String> lines) {
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            sb.append(StringUtils.chomp(line));
            sb.append(line.length() < 27 ? " " : "");
        }
        return WordUtils.capitalizeFully(sb.toString().trim(), ' ', '/');
    }

    public static List<String> splitEqually(String text, int size) {
        if (text == null || text.length() == 0) {
            return new ArrayList<>();
        }

        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }


}
