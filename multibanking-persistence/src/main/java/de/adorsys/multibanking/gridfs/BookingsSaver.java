package de.adorsys.multibanking.gridfs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.encrypt.EncryptionUtil;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Inmemory representation of all bookings of a user
 * Used to read and write bookings
 *
 * @author Christian Brandenstein
 */
public class BookingsSaver {
    private static final Logger LOG = LoggerFactory.getLogger(BookingsSaver.class);

    private List<BookingEntity> bookings;
    private String userId;
    private SecretKey secretKey;

    private GridFSOperations gridFSOperations;

    private Set<String> dirtyQuartale;

    public BookingsSaver(String userId, GridFSOperations gridFSOperations, SecretKey secretKey) {
        this.secretKey = secretKey;

        if (userId == null) {
            throw new IllegalArgumentException("userId null");
        }
        if (gridFSOperations == null) {
            throw new IllegalArgumentException("gridFSSpeicher null");
        }

        this.userId = userId;

        this.gridFSOperations = gridFSOperations;
        this.dirtyQuartale = new HashSet<>();

        readBookingEntityFile();
        sortList();
    }

    public void save(BookingEntity bookingEntity) {
        if (bookings.contains(bookingEntity)) {
            bookings.remove(bookingEntity); // remove old value
        }
        if (bookingEntity.getId() == null) {
            bookingEntity.id(new ObjectId().toHexString());
        }

        bookings.add(bookingEntity);
        dirtyQuartale.add(toQuarter(bookingEntity.getBookingDate()));
    }

    /**
     * Write to GridFS
     * @param force ignore dirty-state und write all
     */
    public void flush(boolean force) {
        if (bookings == null || bookings.isEmpty()) {
            LOG.info("delete bookings file: {}", userId);
            gridFSOperations.filenames(userId).forEach( f ->
                gridFSOperations.delete(f)
            );
            return;
        }

        LOG.info("Count of stored bookings: {}", bookings.size());

        // sort list
        sortList();

        Map<String, List<BookingEntity>> bookingsPerQuater = new HashMap<>();
        // sort quartals
        bookings.forEach(u -> {
            LocalDate bookingDate = u.getBookingDate();
            if (bookingDate == null) {
                return;
            }

            String quartalYear = toQuarter(bookingDate);

            List<BookingEntity> list = bookingsPerQuater.get(quartalYear);
            if (list == null) {
                bookingsPerQuater.put(quartalYear, new ArrayList<>());
            }

            bookingsPerQuater.get(quartalYear).add(u);
        });


        for (String quarter : bookingsPerQuater.keySet()) {
            if( !dirtyQuartale.contains(quarter) && !force ) {
                continue; //BookingEntityliste unchanged
            }

            // Serialize in JSON
            ObjectMapper mapper = getObjectMapper();
            String filename = userId + "_" + quarter;

            try {
                LOG.info("Writing BookingEntity-File {}", filename);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mapper.writeValue(baos, bookingsPerQuater.get(quarter));

                // Encrypt
                String encrypted = EncryptionUtil.encrypt(baos.toString("UTF-8"), secretKey);

                // Store in GridFS
                gridFSOperations.store(filename, encrypted.getBytes("UTF-8"));
            } catch (IOException e) {
                LOG.error("Error writing booking file", e);
            }
        }
    }

    private void sortList() {
        Collections.sort(bookings, new Comparator<BookingEntity>() {
            public int compare(BookingEntity o1, BookingEntity o2) {
                if (o2.getBookingDate() == null) {
                    return -1;
                }
                if (o1.getBookingDate() == null) {
                    return 1;
                }
                return o2.getBookingDate().compareTo(o1.getBookingDate());
            }
        });
    }

    private String toQuarter(LocalDate date) {
        int m = date.getMonth().getValue();
        String quartal = null;
        switch (m) {
            case 1:
            case 2:
            case 3:
                quartal = "Q1";
                break;
            case 4:
            case 5:
            case 6:
                quartal = "Q2";
                break;
            case 7:
            case 8:
            case 9:
                quartal = "Q3";
                break;
            case 10:
            case 11:
            case 12:
                quartal = "Q4";
        }
        int year = date.getYear();
        return quartal + "_" + year;
    }

    private void readBookingEntityFile() {
        ObjectMapper mapper = getObjectMapper();
        LOG.info("Read file for {}", userId);
        bookings = new ArrayList<>();

        List<String> filenames = gridFSOperations.filenames(userId);
        filenames.forEach( f -> {
            try {
                LOG.debug("Read file for {}", f);
                String inputEncrypted = IOUtils.toString(gridFSOperations.read(f), "UTF-8");
                String input = EncryptionUtil.decrypt(inputEncrypted, secretKey);

                if (input != null) {
                    bookings.addAll(mapper.readValue(input, new TypeReference<List<BookingEntity>>() {
                    }));
                }
            } catch (IOException e) {
                LOG.error("Error reading file {}", f, e);
            }
        } );

    }

    public List<BookingEntity> getBookingEntityList() {
        return bookings;
    }

    /**
     * remove ohne flush
     * @param bookingEntity
     */
    public void remove(BookingEntity bookingEntity) {
        this.bookings.remove(bookingEntity);
        dirtyQuartale.add(toQuarter(bookingEntity.getBookingDate()));
    }

    private ObjectMapper getObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }
}
