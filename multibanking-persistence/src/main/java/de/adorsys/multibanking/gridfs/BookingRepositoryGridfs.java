package de.adorsys.multibanking.gridfs;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.encrypt.UserSecret;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import de.adorsys.multibanking.repository.BankAccountRepositoryMongodb;
import domain.BankApi;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile({"mongo-gridfs"})
@Service
public class BookingRepositoryGridfs implements BookingRepositoryIf {
    private static final String ENCRYPTION_METHOD = "AES";

    @Value("${db_secret}")
    private String databaseSecret;

    @Autowired
    private UserSecret userSecret;

    @Autowired
    private GridFSOperations gridFSOperations;

    @Autowired
    private BankAccountRepositoryMongodb bankAccountRepositoryMongodb;

	@Override
	public List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId,
			BankApi bankApi) {
        BookingsSaver bookingsSaver = new BookingsSaver(userId, gridFSOperations, secretKey());
        List<BookingEntity> bookingEntities = bookingsSaver.getBookingEntityList();

        List<BookingEntity> filtered = bookingEntities.stream()
                .filter(b -> b.getAccountId().equals(bankAccountId))
                .filter(b -> b.getBankApi().equals(bankApi))
                .collect(Collectors.toList());

        return filtered;
	}

	@Override
	public Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId) {
        BookingsSaver bookingsSaver = new BookingsSaver(userId, gridFSOperations, secretKey());
        List<BookingEntity> bookingEntities = bookingsSaver.getBookingEntityList();

        Optional<BookingEntity> filtered = bookingEntities.stream()
                .filter(b -> b.getId().equals(bookingId))
                .findFirst();

		return filtered;
	}

	@Override
	public List<BookingEntity> save(List<BookingEntity> bookingEntities) {
        BookingsSaver bookingsSaver = new BookingsSaver(bookingEntities.get(0).getUserId(), gridFSOperations, secretKey());
        bookingEntities.forEach(b -> bookingsSaver.save(b));
        bookingsSaver.flush(false);
        return bookingsSaver.getBookingEntityList();
    }

	@Override
	public void deleteByAccountId(String id) {
        // bankAccountEntity => userId
        BankAccountEntity bankAccountEntity = bankAccountRepositoryMongodb.findOne(id);
        String userId = bankAccountEntity.getUserId();

        BookingsSaver bookingsSaver = new BookingsSaver(userId, gridFSOperations, secretKey());
        List<BookingEntity> toDelete = bookingsSaver.getBookingEntityList()
                .stream()
                .filter(b -> b.getAccountId().equals(id))
                .collect(Collectors.toList());

        toDelete.forEach(b -> bookingsSaver.remove(b));
        bookingsSaver.flush(false);
	}

    private SecretKey secretKey() {
        return new SecretKeySpec(getUserSecret().getBytes(), ENCRYPTION_METHOD);
    }

    private String getUserSecret() {
        try {
            if (userSecret.getSecret() == null) {
                return databaseSecret;
            }
            return userSecret.getSecret();
            //user secret not available outside request scopes
        } catch (BeanCreationException e) {
            return databaseSecret;
        }
    }

}
