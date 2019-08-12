package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;

public class TestUtil {

    static BankEntity getBankEntity(String name, String bankCode, BankApi bankApi) {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setName(name);
        bankEntity.setBankCode(bankCode);
        bankEntity.setSearchIndex(Arrays.asList(name.toLowerCase(), bankCode));
        bankEntity.setBankApi(bankApi);
        return bankEntity;
    }

    static UserEntity getUserEntity(String id) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setExpireUser(LocalDateTime.now().plusMinutes(120));
        return userEntity;
    }

    static BankAccessEntity getBankAccessEntity(String userId, String id, String bankCode, String pin) {
        BankAccessEntity bankAccessEntity = new BankAccessEntity();
        bankAccessEntity.setUserId(userId);
        bankAccessEntity.setId(id);
        bankAccessEntity.setBankCode(bankCode);
        bankAccessEntity.setPin(pin);
        return bankAccessEntity;
    }

    static BankAccountEntity getBankAccountEntity(String id) {
        BankAccountEntity bankAccountEntity = new BankAccountEntity();
        bankAccountEntity.setId(id);
        return bankAccountEntity;
    }

    static Booking createBooking() {
        Booking booking = new Booking();
        booking.setExternalId(UUID.randomUUID().toString());
        booking.setBookingDate(LocalDate.now());
        booking.setAmount(new BigDecimal("20"));

        return booking;
    }

    static TanTransportType createTanMethod(String name) {
        return TanTransportType.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .build();
    }
}
