package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserBookingRecord;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import de.adorsys.multibanking.pers.utils.ObjectPersistenceAdapter;
import de.adorsys.multibanking.pers.utils.UserDataNamingPolicy;
import domain.BankApi;

@Service
public class BookingRepositoryImpl implements BookingRepositoryIf {

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
		throw new UnsupportedOperationException();
	}

	@Override
	public List<BookingEntity> save(List<BookingEntity> bookingEntities) {
        Map<String, UserBookingRecord> bookingRecords = new HashMap<>();
        Map<String, ObjectHandle> bookingHandles = new HashMap<>();
        for (BookingEntity bookingEntity : bookingEntities) {
            String accountId = bookingEntity.getAccountId();
            BankApi bankApi = bookingEntity.getBankApi();
            ObjectHandle bookingsHandle = namingPolicy.handleForBookings(keyCredentials, accountId, bankApi);
            UserBookingRecord userBookingRecord = bookingRecords.get(bookingsHandle.getName());
            if(userBookingRecord==null){
                userBookingRecord = objectPersistenceAdapter.load(bookingsHandle, UserBookingRecord.class, keyCredentials);
                if(userBookingRecord==null){
                    userBookingRecord = new UserBookingRecord();
                }
                bookingRecords.put(bookingsHandle.getName(), userBookingRecord);
                bookingHandles.put(bookingsHandle.getName(), bookingsHandle);
            }
            addBooking(userBookingRecord, bookingEntity);
        }
        Set<Entry<String,UserBookingRecord>> entrySet = bookingRecords.entrySet();
        for (Entry<String, UserBookingRecord> entry : entrySet) {
            objectPersistenceAdapter.store(bookingHandles.get(entry.getKey()), entry.getValue(), keyCredentials);
        }
        return bookingEntities;
	}

	@Override
	public void deleteByAccountId(String id) {
		//TODO
	}

	private void addBooking(UserBookingRecord userBookingRecord, BookingEntity booking){
		ListUtils.add(booking, userBookingRecord.getBookings(), handler);
	}

	static ListItemHandler<BookingEntity> handler = new ListItemHandler<BookingEntity>() {

		@Override
		public boolean idEquals(BookingEntity a, BookingEntity b) {
			return StringUtils.equals(a.getId(), b.getId());
		}

		@Override
		public boolean newId(BookingEntity a) {
			if(StringUtils.isNoneBlank(a.getId())) return false;
			a.setId(UUID.randomUUID().toString());
			return true;
		}
	};
	
}
