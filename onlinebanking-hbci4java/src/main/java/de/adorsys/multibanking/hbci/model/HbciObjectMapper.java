/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.multibanking.hbci.model;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.AbstractRequest;
import de.adorsys.multibanking.domain.transaction.StandingOrder;
import de.adorsys.multibanking.domain.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.adorsys.multibanking.domain.utils.Utils.extractIban;

@Mapper
public interface HbciObjectMapper {

    HbciDialogRequest toHbciDialogRequest(AbstractRequest transactionRequest, HbciCallback callback);

    default BalancesReport createBalancesReport(GVRSaldoReq gvSaldoReq, String accountNumber) {
        return gvSaldoReq.getEntries().stream()
            .filter(info -> info.konto.number.equals(accountNumber))
            .findAny()
            .map(this::toBalancesReport)
            .orElse(null);
    }

    @Mapping(target = "readyBalance", source = "ready")
    @Mapping(target = "availableBalance", source = "available")
    @Mapping(target = "creditBalance", source = "kredit")
    @Mapping(target = "unreadyBalance", source = "unready")
    @Mapping(target = "usedBalance", source = "used")
    BalancesReport toBalancesReport(GVRSaldoReq.Info saldoInfo);

    @Mapping(target = "currency", source = "curr")
    @Mapping(target = "amount", source = "bigDecimalValue")
    @Mapping(target = "date", ignore = true)
    Balance toBalance(Value value);

    @Mapping(target = "currency", source = "value.curr")
    @Mapping(target = "amount", source = "value.bigDecimalValue")
    @Mapping(target = "date", source = "timestamp")
    Balance toBalance(Saldo saldo);

    default List<StandingOrder> createStandingOrders(GVRDauerList gvrDauerList) {
        GVRDauerList.Dauer[] lines = gvrDauerList.getEntries();
        List<StandingOrder> standingOrders = new ArrayList<>();

        for (GVRDauerList.Dauer line : lines) {
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

    default List<Booking> createBookings(GVRKUms gvrkUms) {
        List<Booking> bookings = new ArrayList<>();
        List<GVRKUms.UmsLine> lines = gvrkUms.getFlatData();
        for (GVRKUms.UmsLine line : lines) {
            Booking booking = toBooking(line);
            if (line != null && line.other != null) {
                booking.setOtherAccount(toBankAccount(line.other));

                String differentInitiator = Utils.extractDifferentInitiator(booking.getUsage());
                if (differentInitiator != null) {
                    booking.getOtherAccount().setOwner(booking.getOtherAccount().getOwner() + " " + differentInitiator);
                }

                if (StringUtils.isBlank(booking.getOtherAccount().getIban())) {
                    booking.getOtherAccount().setIban(extractIban(booking.getUsage()));
                }

            }

            bookings.add(0, booking);
        }

        return bookings;
    }

    @Mapping(target = "accountNumber", source = "number")
    @Mapping(target = "currency", source = "curr")
    @Mapping(target = "name", source = "type")
    @Mapping(target = "owner", expression = "java((konto.name + (konto.name2 != null ? konto.name2 : \"\")).trim())")
    @Mapping(target = "type", expression = "java(de.adorsys.multibanking.domain.BankAccountType.fromHbciType(org" +
        ".apache.commons.lang3.math.NumberUtils.toInt(konto.acctype)))")
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "bankName", ignore = true)
    @Mapping(target = "externalIdMap", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "lastSync", ignore = true)
    BankAccount toBankAccount(Konto konto);

    @Mapping(target = "bankApi", constant = "HBCI")
    @Mapping(target = "bookingDate", source = "bdate")
    @Mapping(target = "valutaDate", source = "valuta")
    @Mapping(target = "amount", source = "value.bigDecimalValue")
    @Mapping(target = "currency", source = "value.curr")
    @Mapping(target = "reversal", source = "storno")
    @Mapping(target = "transactionCode", source = "purposecode")
    @Mapping(target = "balance", source = "saldo.value.bigDecimalValue")
    @Mapping(target = "externalId", expression = "java(\"B-\" + line.valuta.getTime() + \"_\" + line.value" +
        ".getLongValue() + \"_\" + line.saldo.value.getLongValue())")
    @Mapping(target = "origValue", source = "orig_value.bigDecimalValue")
    @Mapping(target = "chargeValue", source = "charge_value.bigDecimalValue")
    @Mapping(target = "creditorId", expression = "java(de.adorsys.multibanking.domain.utils.Utils.extractCreditorId" +
        "(booking.getUsage()))")
    @Mapping(target = "mandateReference", expression = "java(de.adorsys.multibanking.domain.utils.Utils" +
        ".extractMandateReference(booking.getUsage()))")
    @Mapping(target = "otherAccount", ignore = true)
    @Mapping(target = "standingOrder", ignore = true)
    @Mapping(target = "bookingCategory", ignore = true)
    Booking toBooking(GVRKUms.UmsLine line);

    default String cycleToTurnus(Cycle cycle) {
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
            case INVALID:
                return null;
            default:
                return null;
        }
    }

    default String cycleToTimeunit(Cycle cycle) {
        switch (cycle) {
            case WEEKLY:
            case TWO_WEEKLY:
                return "W";
            default:
                return "M";
        }
    }

    default String getUsage(List<String> lines) {
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

    default List<String> splitEqually(String text, int size) {
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

    HBCITwoStepMechanism toSecMechInfo(TanTransportType selectedMethod);
}
