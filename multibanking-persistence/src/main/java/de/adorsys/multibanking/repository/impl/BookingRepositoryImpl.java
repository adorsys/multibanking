package de.adorsys.multibanking.repository.impl;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIF;
import de.adorsys.multibanking.repository.BookingRepository;
import domain.BankApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class BookingRepositoryImpl implements BookingRepositoryIF {
	@Autowired
	BookingRepository bookingRepository;

	@Override
	public List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId,
			BankApi bankApi) {
		return bookingRepository.findByUserIdAndAccountIdAndBankApi(userId, bankAccountId, bankApi);
	}

	@Override
	public Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId) {
		return bookingRepository.findByUserIdAndId(userId, bookingId);
	}

	@Override
	public void insert(List<BookingEntity> bookingEntities) {
		bookingRepository.insert(bookingEntities);
	}

}
