package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import de.adorsys.multibanking.repository.BookingPageableRepositoryMongodb;
import de.adorsys.multibanking.repository.BookingRepositoryMongodb;
import domain.BankApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile({"mongo", "fongo"})
@Service
public class BookingRepositoryImpl implements BookingRepositoryIf {

	@Autowired
    private BookingRepositoryMongodb bookingRepository;
	@Autowired
	private BookingPageableRepositoryMongodb bookingPageableRepositoryMongodb;

	@Override
	public Page<BookingEntity> findPageableByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId, String bankAccountId, BankApi bankApi) {
		return bookingPageableRepositoryMongodb.findByUserIdAndAccountIdAndBankApi(pageable, userId, bankAccountId, bankApi);
	}

	@Override
	public List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId,
			BankApi bankApi) {
		return bookingRepository.findByUserIdAndAccountIdAndBankApi(userId, bankAccountId, bankApi, new Sort(Sort.Direction.DESC, "valutaDate"));
	}

	@Override
	public Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId) {
		return bookingRepository.findByUserIdAndId(userId, bookingId);
	}

	@Override
	public Iterable<BookingEntity> findByUserIdAndIds(String name, List<String> ids) {
        return bookingRepository.findAllById(ids);
	}


	@Override
	public List<BookingEntity> save(List<BookingEntity> bookingEntities) {
        List<BookingEntity> newEntities = bookingEntities
                .stream()
                .filter(bookingEntity -> bookingEntity.getId() == null)
                .collect(Collectors.toList());
        try {
            bookingRepository.insert(newEntities);
        } catch (DuplicateKeyException e) {
            //ignore it
        }

        List<BookingEntity> existingEntities = bookingEntities
                .stream()
                .filter(bookingEntity -> bookingEntity.getId() != null)
                .collect(Collectors.toList());

		return bookingRepository.saveAll(existingEntities);
	}

	@Override
	public void deleteByAccountId(String id) {
		bookingRepository.deleteByAccountId(id);
	}


}
