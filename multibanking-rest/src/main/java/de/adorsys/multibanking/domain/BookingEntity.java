package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.Booking;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BookingEntity extends Booking implements IdentityIf {
  private String id;
  private String accountId;
  private String userId;
  private String filePeriod;

  public BookingEntity id(String id) {
    this.id = id;
    return this;
  }
}
