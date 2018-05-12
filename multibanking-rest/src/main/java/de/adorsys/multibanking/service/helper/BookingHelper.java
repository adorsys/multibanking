package de.adorsys.multibanking.service.helper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.springframework.beans.BeanUtils;

import de.adorsys.multibanking.domain.AccountSynchPref;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import domain.Booking;

public class BookingHelper {

    public static Map<String, List<BookingEntity>> mapBookings( 
    		BankAccountEntity bankAccount, AccountSynchPref accountSynchPref, List<? extends Booking> bookings) {
        return bookings.stream()
                .map(booking -> {
                    BookingEntity target = new BookingEntity();
                    BeanUtils.copyProperties(booking, target);
                    target.setAccountId(bankAccount.getId());
                    target.setUserId(bankAccount.getUserId());
                    return target;
                })
                .collect(Collectors.groupingBy(booking -> period(booking, accountSynchPref)));
    }

    public static Map<String, List<BookingEntity>> reMapBookings(List<BookingEntity> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(BookingEntity::getFilePeriod));
    }

    /**
     * Associates booking with period by booking date.
     * 
     * @param b
     * @return
     */
    public static String period(BookingEntity b, AccountSynchPref pref) {
    	LocalDate bookingDate = b.getBookingDate();
    	if(bookingDate==null) throw new BaseException("Missing booking date for booking: " + b.getId());
    	String period = pref.getBookingPeriod().marker(bookingDate);
    	b.setFilePeriod(period);
    	return period;
	}
}
