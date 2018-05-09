package de.adorsys.multibanking.service.analytics;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.smartanalytics.api.Booking;
import de.adorsys.smartanalytics.api.BookingGroup;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.WrappedBooking;
import domain.BookingCategory;
import domain.Contract;
import domain.Cycle;
import domain.RuleCategory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SmartanalyticsMapper {

    static List<Booking> convertInput(Collection<BookingEntity> bookings) {
        List<Booking> interfaceBookings = new ArrayList<>();
        for (BookingEntity booking : bookings) {
            Booking smartanalyticsBooking = Booking.builder()
                    .bookingId(booking.getExternalId())
                    .creditorId(booking.getCreditorId())
                    .purpose(booking.getUsage())
                    .iban(booking.getOtherAccount() != null ? booking.getOtherAccount().getIban() : null)
                    .accountNumber(booking.getOtherAccount() != null ? booking.getOtherAccount().getAccountNumber() : null)
                    .bankCode(booking.getOtherAccount() != null ? booking.getOtherAccount().getBlz() : null)
                    .amount(booking.getAmount())
                    .executionDate(booking.getBookingDate())
                    .orderDate(booking.getValutaDate())
                    .type(booking.isStandingOrder() ? Booking.BookingType.STANDING_ORDER : null)
                    .mandateReference(booking.getMandateReference())
                    .build();
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

    public static void applyCategories(List<BookingEntity> bookingEntities, AnalyticsResult result, List<RuleCategory> categories) {
        result.getBookings().forEach(categorizedBooking -> {
            bookingEntities.stream()
                    .filter(bookingEntity -> categorizedBooking.getBooking().getBookingId().equals(bookingEntity.getExternalId()))
                    .findFirst().ifPresent(bookingEntity -> {
                //Überschreibe Gegegenkonto Inhaber mit einem besseren Namen aus Kategorisierung, statt z.b. '2631EDEKA HOFMANN CADOLZBU'
                if (StringUtils.isNotBlank(categorizedBooking.getReceiver()) && bookingEntity.getOtherAccount() != null) {
                    bookingEntity.getOtherAccount().setOwner(categorizedBooking.getReceiver());
                }

                if (categorizedBooking.getMainCategory() != null) {
                    BookingCategory category = mapToBookingcategory(categorizedBooking, categories);
                    category.setVariable(categorizedBooking.isVariable());
                    if (categorizedBooking.getCycle() != null) {
                        category.getContract().setInterval(Cycle.valueOf(categorizedBooking.getCycle().name()));
                    }
                    bookingEntity.setBookingCategory(category);
                }
            });
        });
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
        contractEntity.setProvider(bookingGroup.getProvider());
        contractEntity.setMandateReference(bookingGroup.getMandatreference());

        return contractEntity;
    }

    static BookingCategory mapToBookingcategory(WrappedBooking extendedBooking, List<RuleCategory> categories) {
        BookingCategory bookingCategory = BookingCategory.builder()
                .mainCategory(extendedBooking.getMainCategory())
                .subCategory(extendedBooking.getSubCategory())
                .specification(extendedBooking.getSpecification())
                .rules(extendedBooking.getRuleIds())
                .contract(Contract.builder()
                        .email(extendedBooking.getEmail())
                        .homepage(extendedBooking.getHomepage())
                        .logo(extendedBooking.getLogo())
                        .mandateReference(extendedBooking.getMandateReference())
                        .hotline(extendedBooking.getHotline())
                        .email(extendedBooking.getEmail())
                        .build())
                .build();

        categories.forEach(category -> {
            if (category.getId().equals(bookingCategory.getMainCategory())) {
                bookingCategory.setMainCategoryName(category.getName());

                if (!StringUtils.isEmpty(bookingCategory.getSubCategory()) && category.getSubcategories() != null) {
                    category.getSubcategories().forEach(subCategory -> {
                        if (subCategory.getId().equals(bookingCategory.getSubCategory())) {
                            bookingCategory.setSubCategoryName(subCategory.getName());

                            if (!StringUtils.isEmpty(bookingCategory.getSpecification()) && subCategory.getSpecifications() != null) {
                                subCategory.getSpecifications().forEach(specification -> {
                                    if (specification.getId().equals(bookingCategory.getSpecification())) {
                                        bookingCategory.setSpecificationName(specification.getName());
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        return bookingCategory;
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
                .nextExecutionDate(bookingsGroup.getNextExecutionDate())
                .variable(bookingsGroup.isVariable())
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

        if (bookingsGroup.isVariable()) {
            bookingGroup.setOtherAccount("");
        } else {
            bookingGroup.setOtherAccount(bookingsGroup.getProvider());
        }

        return bookingGroup;
    }


}
