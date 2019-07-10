package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;

public class TestUtil {

    public static UserEntity getUserEntity(String id) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(120));
        return userEntity;
    }

    public static BankAccessEntity getBankAccessEntity(String userId, String id, String bankCode, String pin) {
        BankAccessEntity bankAccessEntity = new BankAccessEntity();
        bankAccessEntity.setUserId(userId);
        bankAccessEntity.setId(id);
        bankAccessEntity.setBankCode(bankCode);
        bankAccessEntity.setPin(pin);
        return bankAccessEntity;
    }

    public static BankAccountEntity getBankAccountEntity(String id) {
        BankAccountEntity bankAccountEntity = new BankAccountEntity();
        bankAccountEntity.setId(id);
        return bankAccountEntity;
    }

    public static BookingEntity getBookingEntity(String userId, String accountId, BankApi bankApi) {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setUserId(userId);
        bookingEntity.setAccountId(accountId);
        bookingEntity.setBankApi(bankApi);
        return bookingEntity;
    }

    public static BankEntity getBankEntity(String name, String bankCode) {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setName(name);
        bankEntity.setBankCode(bankCode);
        bankEntity.setSearchIndex(Arrays.asList(name.toLowerCase(), bankCode));
        return bankEntity;

    }

}
