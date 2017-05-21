package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserBookingRecord;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIF;
import de.adorsys.multibanking.pers.utils.ObjectPersistenceAdapter;
import de.adorsys.multibanking.pers.utils.UserDataNamingPolicy;
import domain.BankApi;

@Service
public class BookingRepositoryImpl implements BookingRepositoryIF {

    @Autowired
    private KeyCredentials keyCredentials;
    
    @Autowired
    private UserDataNamingPolicy namingPolicy;

    @Autowired
    private ObjectPersistenceAdapter objectPersistenceAdapter;
    
	@Override
	public List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId,
			BankApi bankApi) {
		ObjectHandle bookingsHandle = namingPolicy.handleForBookings(keyCredentials, bankAccountId, bankApi);
		UserBookingRecord userBookingRecord = objectPersistenceAdapter.load(bookingsHandle, UserBookingRecord.class, keyCredentials);
		if(userBookingRecord==null) return Collections.emptyList();
		return userBookingRecord.getBookings();
	}

	@Override
	public Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(List<BookingEntity> bookingEntities) {
//		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
//		ObjectHandle bookingsHandle = namingPolicy.handleForBookings(keyCredentials, bankAccountId, bankApi);
	}

}
