package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import de.adorsys.multibanking.repository.BookingRepositoryMongodb;
import domain.BankApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BookingRepositoryImpl implements BookingRepositoryIf {
	@Autowired
    BookingRepositoryMongodb bookingRepository;

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
