package de.adorsys.multibanking.domain;

import java.util.Optional;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.Booking;
import domain.BookingCategory;
import domain.Contract;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BookingEntity extends Booking implements IdentityIf {
  private String id;
  private String accountId;
  private String userId;

  public BookingEntity id(String id) {
    this.id = id;
    return this;
  }

  public boolean isContract() {
    return Optional.of(this).map(BookingEntity::getBookingCategory)
        .map(BookingCategory::getContract).map(Contract::getInterval).isPresent();
  }
}
