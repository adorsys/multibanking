package de.adorsys.multibanking.service.old;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.utils.Ids;
import domain.BankApi;

public class TestUtil {

	public static UserEntity getUserEntity(String id){
		UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
		userEntity.setExpireUser(Date.from(LocalDateTime.now().plusMinutes(120).atZone(ZoneId.systemDefault()).toInstant()));
		return userEntity;
	}

	public static BankAccessEntity getBankAccessEntity(String userId, String id, String bankCode, String pin){
		BankAccessEntity bankAccessEntity = new BankAccessEntity();
		bankAccessEntity.setUserId(userId);
		bankAccessEntity.setId(id);
		bankAccessEntity.setBankCode(bankCode);
		bankAccessEntity.setPin(pin);
		return bankAccessEntity;
	}

	public static BankAccountEntity getBankAccountEntity(String id){
		BankAccountEntity bankAccountEntity = new BankAccountEntity();
        bankAccountEntity.setId(id);
		return bankAccountEntity;
	}

	public static BookingEntity getBookingEntity(String userId, String accountId, BankApi bankApi){
		BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setUserId(userId);
        bookingEntity.setAccountId(accountId);
        bookingEntity.setBankApi(bankApi);
		return bookingEntity;
	}

	public static BankAccountEntity getBankAccountEntity(BankAccessEntity accessEntity, String accountId) {
		BankAccountEntity accountEntity = getBankAccountEntity(accountId);
		accountEntity.setBlz(accessEntity.getBankCode());
		accountEntity.setBankAccessId(accessEntity.getId());
		accountEntity.setUserId(accessEntity.getUserId());
		return accountEntity;
	}

	public static BookingEntity getBookingEntity(BankAccountEntity accountEntity, BankApi bankApi, LocalDate bookingDate){
		BookingEntity bookingEntity = new BookingEntity();
		bookingEntity.setId(Ids.uuid());
        bookingEntity.setUserId(accountEntity.getUserId());
        bookingEntity.setAccountId(accountEntity.getId());
        bookingEntity.setBankApi(bankApi);
        bookingEntity.setBookingDate(bookingDate);
		return bookingEntity;
	}
}
