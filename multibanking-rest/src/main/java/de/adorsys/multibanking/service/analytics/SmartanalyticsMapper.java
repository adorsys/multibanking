package de.adorsys.multibanking.service.analytics;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.smartanalytics.api.*;
import domain.BookingCategory;
import domain.Contract;
import domain.Cycle;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SmartanalyticsMapper {

    static List<Booking> convertInput(Collection<BookingEntity> bookings) {
        List<Booking> interfaceBookings = new ArrayList<>();
        for (BookingEntity booking : bookings) {
            Booking smartanalyticsBooking = new Booking();
            smartanalyticsBooking.setBookingId(booking.getExternalId());
            smartanalyticsBooking.setCreditorId(booking.getCreditorId());
            smartanalyticsBooking.setPurpose(booking.getUsage());
            smartanalyticsBooking.setIban(booking.getOtherAccount() != null ? booking.getOtherAccount().getIban() : null);
            smartanalyticsBooking.setAccountNumber(booking.getOtherAccount() != null ? booking.getOtherAccount().getAccountNumber() : null);
            smartanalyticsBooking.setBankCode(booking.getOtherAccount() != null ? booking.getOtherAccount().getBlz() : null);
            smartanalyticsBooking.setAmount(booking.getAmount());
            smartanalyticsBooking.setExecutionDate(booking.getValutaDate());
            smartanalyticsBooking.setStandingOrder(booking.isStandingOrder());
            smartanalyticsBooking.setMandateReference(booking.getMandateReference());

            if (booking.getOtherAccount() != null) {
                if (booking.getOtherAccount().getOwner() != null) {
                    smartanalyticsBooking.setReferenceName(booking.getOtherAccount().getOwner());
                } else {
                    smartanalyticsBooking.setReferenceName(booking.getOtherAccount().getName());
                }
            }
            interfaceBookings.add(smartanalyticsBooking);
        }
        return interfaceBookings;
    }

    public static void applyCategories(List<BookingEntity> bookingEntities, AnalyticsResult result) {
        result.getBookings().forEach(categorizedBooking -> {
            bookingEntities.stream()
                    .filter(bookingEntity -> categorizedBooking.getBooking().getBookingId().equals(bookingEntity.getExternalId()))
                    .findFirst().ifPresent(bookingEntity -> {
                if (categorizedBooking.getMainCategory() != null) {
                    bookingEntity.setBookingCategory(mapToBookingcategory(categorizedBooking));
                }
            });
        });
    }

    static BookingCategory mapToBookingcategory(WrappedBooking wrappedBooking) {
        BookingCategory bookingCategory = new BookingCategory();
        bookingCategory.setReceiver(wrappedBooking.getOtherAccount());
        bookingCategory.setMainCategory(wrappedBooking.getMainCategory());
        bookingCategory.setSubCategory(wrappedBooking.getSubCategory());
        bookingCategory.setSpecification(wrappedBooking.getSpecification());
        bookingCategory.setCustom(wrappedBooking.getCustom());

        bookingCategory.setEmail(wrappedBooking.getEmail());
        bookingCategory.setHomepage(wrappedBooking.getHomepage());
        bookingCategory.setHotline(wrappedBooking.getHotline());
        bookingCategory.setLogo(wrappedBooking.getLogo());

        if (wrappedBooking.getCycle() != null) {
            bookingCategory.setInterval(Cycle.valueOf(wrappedBooking.getCycle().toString()));
        }

        bookingCategory.setRules(wrappedBooking.getRuleIds());
        return bookingCategory;
    }


    static ContractEntity toContract(String userId, String accountId, BookingGroup bookingGroup) {
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setAmount(bookingGroup.getAmount());
        contractEntity.setInterval(Cycle.valueOf(bookingGroup.getCycle().toString()));

        contractEntity.setUserId(userId);
        contractEntity.setAccountId(accountId);
        contractEntity.setEmail(bookingGroup.getEmail());
        contractEntity.setHomepage(bookingGroup.getHomepage());
        contractEntity.setHotline(bookingGroup.getHotline());
        contractEntity.setLogo(bookingGroup.getLogo());

        contractEntity.setMainCategory(bookingGroup.getMainCategory());
        contractEntity.setSubCategory(bookingGroup.getSubCategory());
        contractEntity.setSpecification(bookingGroup.getSpecification());
        contractEntity.setProvider(bookingGroup.getOtherAccount());
        contractEntity.setMandateReference(bookingGroup.getMandatreference());

        return contractEntity;
    }

    static List<de.adorsys.multibanking.domain.BookingGroup> mapBookingGroups(List<BookingGroup> bookingGroups) {
        return bookingGroups.stream()
                .map(bookingGroup -> toBookingGroup(bookingGroup))
                .collect(Collectors.toList());
    }

    static de.adorsys.multibanking.domain.BookingGroup toBookingGroup(BookingGroup bookingsGroup) {
        de.adorsys.multibanking.domain.BookingGroup bookingGroup = de.adorsys.multibanking.domain.BookingGroup.builder()
                .amount(bookingsGroup.getAmount())
                .mainCategory(bookingsGroup.getMainCategory())
                .subCategory(bookingsGroup.getSubCategory())
                .specification(bookingsGroup.getSpecification())
                .bookingPeriods(mapBookingPeriods(bookingsGroup.getBookingPeriods()))
                .type(de.adorsys.multibanking.domain.BookingGroup.Type.valueOf(bookingsGroup.getGroupType().toString()))
                .contract(Contract.builder()
                        .email(bookingsGroup.getEmail())
                        .homepage(bookingsGroup.getHomepage())
                        .logo(bookingsGroup.getLogo())
                        .mandateReference(bookingsGroup.getMandatreference())
                        .hotline(bookingsGroup.getHotline())
                        .email(bookingsGroup.getEmail())
                        .interval(bookingsGroup.getCycle() != null ? Cycle.valueOf(bookingsGroup.getCycle().name()) : null)
                        .build()
                )
                .build();

        if (bookingsGroup.getGroupType() == Group.Type.CUSTOM || bookingsGroup.getGroupType() == Group.Type.OTHER_INCOME
                || bookingsGroup.getGroupType() == Group.Type.OTHER_EXPENSES) {
            bookingGroup.setOtherAccount("");
        } else {
            bookingGroup.setOtherAccount(bookingsGroup.getOtherAccount());
        }

        return bookingGroup;
    }

    static List<de.adorsys.multibanking.domain.BookingPeriod> mapBookingPeriods(List<BookingPeriod> bookingPeriods) {
        if (bookingPeriods == null) {
            return null;
        }

        return bookingPeriods.stream()
                .map(bookingPeriod -> toBookingPeriod(bookingPeriod))
                .collect(Collectors.toList());
    }

    private static de.adorsys.multibanking.domain.BookingPeriod toBookingPeriod(BookingPeriod bookingPeriod) {
        de.adorsys.multibanking.domain.BookingPeriod period = de.adorsys.multibanking.domain.BookingPeriod.builder().build();
        BeanUtils.copyProperties(bookingPeriod, period);
        return period;
    }


}
