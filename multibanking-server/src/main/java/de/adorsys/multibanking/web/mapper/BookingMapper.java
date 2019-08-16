package de.adorsys.multibanking.web.mapper;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.web.model.BookingTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "otherAccount.id", ignore = true)
    @Mapping(target = "otherAccount.userId", ignore = true)
    @Mapping(target = "otherAccount.bankAccessId", ignore = true)
    @Mapping(target = "bookingCategory.id", ignore = true)
    BookingTO toBookingTO(BookingEntity bookingEntity);

    List<BookingTO> toBookingTOs(Iterable<BookingEntity> bookingEntities);
}
