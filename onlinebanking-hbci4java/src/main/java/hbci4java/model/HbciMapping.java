/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hbci4java.model;


import domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import utils.Utils;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.Utils.extractIban;

/**
 * Created by alexg on 08.02.17.
 */
@Slf4j
public final class HbciMapping {

    public static BalancesReport createBalance(GVRSaldoReq gvSaldoReq) {
        BalancesReport result = new BalancesReport();
        if (gvSaldoReq.isOK()) {
            GVRSaldoReq.Info[] infos = gvSaldoReq.getEntries();
            if (infos.length > 0) {
                if (infos[0] != null && infos[0].ready != null && infos[0].ready.value != null) {
                    result.setReadyHbciBalance(createBalance(infos[0].ready));
                }
                if (infos[0] != null && infos[0].available != null) {
                    result.setAvailableHbciBalance(createBalance(infos[0].available));
                }
                if (infos[0] != null && infos[0].kredit != null) {
                    result.setCreditHbciBalance(createBalance(infos[0].kredit));
                }
                if (infos[0] != null && infos[0].unready != null && infos[0].unready.value != null) {
                    result.setUnreadyHbciBalance(createBalance(infos[0].unready));
                }
                if (infos[0] != null && infos[0].used != null) {
                    result.setUsedHbciBalance(createBalance(infos[0].used));
                }
            }
        }
        return result;
    }

    private static Balance createBalance(Value value) {
        return Balance.builder()
                .currency(value.getCurr())
                .amount(value.getBigDecimalValue().setScale(2))
                .build();
    }

    private static Balance createBalance(Saldo saldo) {
        return Balance.builder()
                .date(saldo.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .currency(saldo.value.getCurr())
                .amount(saldo.value.getBigDecimalValue().setScale(2))
                .build();
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
                    log.warn("Booking has no amount, skipping: %s", line);
                    continue;
                }
                if (line.bdate == null) {
                    log.warn("Booking has no booking date, skipping: %s", line);
                    continue;
                }
                Booking booking = new Booking();
                booking.setBankApi(BankApi.HBCI);
                booking.setBookingDate(line.bdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                booking.setAmount(line.value.getBigDecimalValue().setScale(2));
                booking.setCurrency(line.value.getCurr());
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

                    String differentInitiator = Utils.extractDifferentInitiator(booking.getUsage());
                    if (differentInitiator != null) {
                        booking.getOtherAccount().setOwner(booking.getOtherAccount().getOwner() + " " + differentInitiator);
                    }

                    if (StringUtils.isBlank(booking.getOtherAccount().getIban())) {
                        booking.getOtherAccount().setIban(extractIban(booking.getUsage()));
                    }

                }
                booking.setExternalId("B-" + line.bdate.getTime() + "_" + line.value.getLongValue()
                        + "_" + line.saldo.value.getLongValue());

                // die Bank liefert keine strukturierten Verwendungszwecke (gvcode=999).
                // Daher verwenden wir den gesamten "additional"-Block und zerlegen ihn
                // in 27-Zeichen lange Haeppchen
                booking.setUsage(
                        getUsage(line.usage.size() > 0 ? line.usage : splitEqually(line.additional, 27)));

                booking.setCreditorId((Utils.extractCreditorId(booking.getUsage())));
                booking.setMandateReference(Utils.extractMandateReference(booking.getUsage()));

                bookings.add(0, booking);
            }
        }

        log.debug("Received {} bookings: {}", bookings.size(), bookings);
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
        bankAccount.rawType(NumberUtils.toInt(konto.acctype));
        return bankAccount;
    }

    public static String cycleToTurnus(Cycle cycle) {
        switch (cycle) {
            case WEEKLY:
                return "1";
            case TWO_WEEKLY:
                return "2";
            case MONTHLY:
                return "1";
            case TWO_MONTHLY:
                return "2";
            case QUARTERLY:
                return "3";
            case HALF_YEARLY:
                return "6";
            case YEARLY:
                return "12";
        }
        return null; //INVALID
    }

    public static String cycleToTimeunit(Cycle cycle) {
        switch (cycle) {
            case WEEKLY:
            case TWO_WEEKLY:
                return "W";
            default:
                return "M";
        }
    }

    private static String getUsage(List<String> lines) {
        StringBuilder sb = new StringBuilder();

        if (lines != null) {
            for (String line : lines) {
                if (line != null) {
                    sb.append(StringUtils.chomp(line));
                    sb.append(line.length() < 27 ? " " : "");
                }
            }
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
