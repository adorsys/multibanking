package de.adorsys.multibanking.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;
import de.adorsys.multibanking.encrypt.Encrypted;
import domain.Booking;
import domain.BookingCategory;
import domain.Contract;
import domain.RuleCategory;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@Document
@CompoundIndexes({@CompoundIndex(name = "booking_index", def = "{'userId': 1, 'accountId': 1}"),
    @CompoundIndex(name = "booking_unique_index", def = "{'externalId': 1, 'accountId': 1}",
        unique = true)})
@Encrypted(
    exclude = {"_id", "accountId", "externalId", "userId", "valutaDate", "bookingDate", "bankApi"})
public class BookingEntity extends Booking {

  @Id
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
