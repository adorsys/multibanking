package de.adorsys.multibanking.service.analytics;

import de.adorsys.multibanking.domain.BookingCategory;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.Contract;
import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.Booking;
import de.adorsys.smartanalytics.api.BookingGroup;
import de.adorsys.smartanalytics.api.WrappedBooking;
import de.adorsys.smartanalytics.api.config.Group;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SmartAnalyticsMapper {

    public void applyCategories(List<BookingEntity> bookingEntities, AnalyticsResult result) {
        result.getBookings().forEach(categorizedBooking ->
            bookingEntities.stream()
                .filter(bookingEntity -> categorizedBooking.getBooking().getBookingId().equals(bookingEntity.getExternalId()))
                .findFirst()
                .ifPresent(bookingEntity -> {
                    if (categorizedBooking.getMainCategory() != null) {
                        bookingEntity.setBookingCategory(toBookingcategory(categorizedBooking));
                    }
                }));
    }

    @Mapping(source = "cycle", target = "interval")
    @Mapping(source = "otherAccount", target = "receiver")
    @Mapping(source = "ruleIds", target = "rules")
    @Mapping(target = "cancelled", ignore = true)
    @Mapping(target = "provider", ignore = true)
    abstract BookingCategory toBookingcategory(WrappedBooking wrappedBooking);

    abstract List<de.adorsys.multibanking.domain.BookingGroup> mapBookingGroups(List<BookingGroup> bookingGroups);

    @Mapping(target = "otherAccount", ignore = true)
    @Mapping(source = "groupType", target = "type")
    @Mapping(source = ".", target = "contract")
    abstract de.adorsys.multibanking.domain.BookingGroup mapBookingGroup(BookingGroup bookingGroup);

    @AfterMapping
    public void mapBookingGroupAfterMapping(BookingGroup bookingsGroup,
                                            @MappingTarget de.adorsys.multibanking.domain.BookingGroup.BookingGroupBuilder bookingGroup) {
        if (bookingsGroup.getGroupType() == Group.Type.CUSTOM || bookingsGroup.getGroupType() == Group.Type.OTHER_INCOME
            || bookingsGroup.getGroupType() == Group.Type.OTHER_EXPENSES) {
            bookingGroup.otherAccount("");
        } else {
            bookingGroup.otherAccount(bookingsGroup.getOtherAccount());
        }
    }

    @Mapping(source = "mandatreference", target = "mandateReference")
    @Mapping(source = "cycle", target = "interval")
    @Mapping(target = "provider", ignore = true)
    abstract Contract toContract(BookingGroup bookingsGroup);

    @Mapping(source = "bookingGroup.mandatreference", target = "mandateReference")
    @Mapping(source = "bookingGroup.cycle", target = "interval")
    @Mapping(source = "bookingGroup.otherAccount", target = "provider")
    @Mapping(target = "id", ignore = true)
    abstract ContractEntity toContractEntity(String userId, String accountId, BookingGroup bookingGroup);

    abstract List<Booking> toSmartAnalyticsBookings(Collection<BookingEntity> bookings);

    @Mapping(target = "referenceName", ignore = true)
    @Mapping(source = "externalId", target = "bookingId")
    @Mapping(source = "usage", target = "purpose")
    @Mapping(source = "valutaDate", target = "executionDate")
    @Mapping(source = "otherAccount.iban", target = "iban")
    @Mapping(source = "otherAccount.accountNumber", target = "accountNumber")
    @Mapping(source = "otherAccount.blz", target = "bankCode")
    abstract Booking toSmartAnalyticsBooking(BookingEntity booking);

    @AfterMapping
    void toSmartAnalyticsBookingAfterMapping(BookingEntity bookingEntity, @MappingTarget Booking booking) {
        if (bookingEntity.getOtherAccount() != null) {
            if (bookingEntity.getOtherAccount().getOwner() != null) {
                booking.setReferenceName(bookingEntity.getOtherAccount().getOwner());
            } else {
                booking.setReferenceName(bookingEntity.getOtherAccount().getName());
            }
        }
    }
}
