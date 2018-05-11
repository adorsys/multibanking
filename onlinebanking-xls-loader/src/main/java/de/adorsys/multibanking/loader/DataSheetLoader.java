package de.adorsys.multibanking.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;

import de.adorsys.multibanking.exception.InvalidRowException;

public class DataSheetLoader {
	private static final Logger LOG = Logger.getLogger(DataSheetLoader.class.getName());

	private BankAccesLoader bankAccesLoader;
	private BankAccountLoader bankAccountLoader;
	private BookingLoader bookingLoader;
	private StandingOrderLoader standingOrderLoader;

	public DataSheetLoader(BankAccesLoader bankAccesLoader, BankAccountLoader bankAccountLoader,
			BookingLoader bookingLoader, StandingOrderLoader standingOrderLoader) {
		super();
		this.bankAccesLoader = bankAccesLoader;
		this.bankAccountLoader = bankAccountLoader;
		this.bookingLoader = bookingLoader;
		this.standingOrderLoader = standingOrderLoader;
	}

	public void loadDataSheet(InputStream dataStream) {
		if (dataStream == null) {
			throw new IllegalArgumentException("dataStream can not be null");
		}
		try {
			HSSFWorkbook workbook = new HSSFWorkbook(dataStream);
			// updateLogin(workbook, userId);
			updateBankAccess(workbook);
			updateBankAccount(workbook);
			updateBooking(workbook);
			updateStandingOrder(workbook);
			IOUtils.closeQuietly(dataStream);

		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(dataStream);
		}

	}

	public void updateBankAccess(HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.getSheet("BankAccess");
		if (sheet == null)
			return;

		Iterator<Row> rowIterator = sheet.rowIterator();
		Row row1 = rowIterator.next();
//		Row row2 = rowIterator.next();
		rowIterator.forEachRemaining(row -> {
			try {
				bankAccesLoader.update(row);
			} catch (InvalidRowException i){
				LOG.severe(i.getMessage());
			}
		});

	}

	public void updateBankAccount(HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.getSheet("BankAccount");
		if (sheet == null)
			return;

		Iterator<Row> rowIterator = sheet.rowIterator();
		rowIterator.next();
//		rowIterator.next();

		rowIterator.forEachRemaining(row -> {
			try {
				bankAccountLoader.update(row);
			} catch (InvalidRowException i){
				LOG.severe(i.getMessage());
			}
		});
	}

	public void updateBooking(HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.getSheet("Booking");
		if (sheet == null)
			return;

		Iterator<Row> rowIterator = sheet.rowIterator();
		System.out.println("roo number " + sheet.getLastRowNum());

		rowIterator.next();
//		rowIterator.next();
		rowIterator.forEachRemaining(row -> {
			try {
				bookingLoader.update(row);
			} catch (InvalidRowException i){
				LOG.severe(i.getMessage());
			}
		});
	}

	private void updateStandingOrder(HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.getSheet("StandingOrder");
		if (sheet == null)
			return;

		Iterator<Row> rowIterator = sheet.rowIterator();
		rowIterator.next();
//		rowIterator.next();
		rowIterator.forEachRemaining(row -> {
			try {
				standingOrderLoader.update(row);
			} catch (InvalidRowException i){
				LOG.severe(i.getMessage());
			}
		});
	}

}
