package de.adorsys.multibanking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.AccountSynchPref;
import de.adorsys.multibanking.domain.AccountSynchResult;
import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.BookingFile;
import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.UnexistentBookingFileException;
import de.adorsys.multibanking.service.analytics.AnalyticsService;
import de.adorsys.multibanking.service.analytics.AnonymizationService;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.helper.BookingHelper;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.FQNUtils;
import domain.BankAccount;
import domain.BankApi;
import domain.BankApiUser;
import domain.Booking;
import domain.LoadBookingsResponse;
import exception.InvalidPinException;
import spi.OnlineBankingService;
import utils.Utils;

/**
 * 
 * @author fpo 2018-03-17 12:16
 *
 */
@Service
public class BookingService {
	
	@Autowired
	private UserObjectService uos;
	
	@Autowired
    private UserDataService uds;
    @Autowired
    private BankAccessService bankAccessService;
    @Autowired
    private BankAccountService bankAccountService;
	@Autowired
	private BankAccessCredentialService credentialService;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private BankService bankService;
    @Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private AnonymizationService anonymizationService;
    
    /**
     * Read and returns the booking file for a given period. Single bookings are not deserialized in
     * the memory of this JVM.
     * 
     * @param accessId
     * @param accountId
     * @param period
     * @return
     */
    public DSDocument getBookings(String accessId, String accountId, String period) {
    	AccountSynchResult accountSynchResult = bankAccountService.loadAccountSynchResult(accessId, accountId);
    	DocumentFQN bookingFQN = FQNUtils.bookingFQN(accessId,accountId,period);
    	if(!accountSynchResult.getBookingFiles().containsKey(period))
    		throw new UnexistentBookingFileException(bookingFQN.getValue());
        return uos.loadDocument(bookingFQN);
    }

    /**
     * - Get additional booking from the remote repository.
     * - Triggers analytics
     * 
     * @param accessId
     * @param accountId
     * @param bankApi
     * @param pin
     */
    public void syncBookings(String accessId, String accountId, BankApi bankApi, String pin) {
    	// Set the synch status and flush
        bankAccountService.updateSyncStatus(accessId, accountId, BankAccount.SyncStatus.SYNC);
        uos.flush();
        
        // Reload
        UserData userData = uds.load();
        BankAccessEntity bankAccess = userData.bankAccessData(accessId).getBankAccess();
        BankAccountEntity bankAccount = userData.bankAccountData(accessId, accountId).getBankAccount();
        try {
            LoadBookingsResponse response = loadBookingsOnline(bankApi, bankAccess, bankAccount, pin);

            bankAccount.setBankAccountBalance(response.getBankAccountBalance());
            if (bankAccess.isStoreBookings()) {
                bankAccount.setLastSync(LocalDateTime.now());
                bankAccountService.saveBankAccount(bankAccount);
            }

            if (!bankAccess.isTemporary()) {
                //update bankaccess, passportstate changed
                bankAccessService.updateBankAccess(bankAccess);
            }

            processBookings(bankAccess, bankAccount, response);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("sync bookings failed", e);
            throw e;
        } finally {
            bankAccountService.updateSyncStatus(bankAccess.getId(), bankAccount.getId(), BankAccount.SyncStatus.PENDING);
        }
    }

    private void processBookings(BankAccessEntity bankAccess, 
    		BankAccountEntity bankAccount, LoadBookingsResponse response) {
    	
    	AccountSynchResult synchResult = bankAccountService.loadAccountSynchResult(bankAccess.getId(), bankAccount.getId());
    	AccountSynchPref accountSynchPref = bankAccountService.findAccountSynchPref(bankAccess.getId(), bankAccount.getId());
    	
        Map<String, List<BookingEntity>> bookings = BookingHelper.mapBookings(bankAccount, accountSynchPref, response.getBookings());
        
        Map<String, BookingFile> bookingFileMap = synchResult.getBookingFiles();
        Set<Entry<String,List<BookingEntity>>> entrySet = bookings.entrySet();
        for (Entry<String, List<BookingEntity>> entry : entrySet) {
        	List<BookingEntity> bookingEntities = entry.getValue();
            bookingEntities.forEach(booking ->
            	response.getStandingOrders()
                    .stream()
                    .filter(so -> so.getAmount().negate().compareTo(booking.getAmount()) == 0 &&
                            Utils.inCycle(booking.getValutaDate(), so.getExecutionDay()) &&
                            Utils.usageContains(booking.getUsage(), so.getUsage())
                    )
                    .findFirst()
                    .ifPresent(standingOrder -> {
                        booking.setOtherAccount(standingOrder.getOtherAccount());
                        booking.setStandingOrder(true);
                    }));
            String period = entry.getKey();
			DocumentFQN bookingFQN = FQNUtils.bookingFQN(bankAccess.getId(),bankAccount.getId(),period);
			List<BookingEntity> existingBookings = uos.load(bookingFQN, listType())
					.orElse(Collections.emptyList());
            bookingEntities = mergeBookings(existingBookings,bookingEntities);

            // Store meta data
            BookingFile bookingFile = bookingFileMap.get(period);
            if(bookingFile==null){
            	bookingFile = new BookingFile();
            	bookingFile.setPeriod(period);
            	bookingFile.setLastUpdate(LocalDateTime.now());
            	synchResult.update(Collections.singletonList(bookingFile));            	
            }
            bookingFile.setNumberOfRecords(bookingEntities.size());
            bankAccountService.storeAccountSynchResult(bankAccess.getId(),bankAccount.getId(), synchResult);

            // Sort and store bookings
            Collections.sort(bookingEntities, (o1, o2) -> o2.getBookingDate().compareTo(o1.getBookingDate()));
            if (bankAccess.isStoreBookings()) {
            	uos.store(bookingFQN, listType(), bookingEntities);
            }
		}
        bankAccountService.saveStandingOrders(bankAccount, response.getStandingOrders());
        bankAccountService.updateSyncStatus(bankAccount.getBankAccessId(), bankAccount.getId(), BankAccount.SyncStatus.READY);
        uos.flush();
        
        if (bankAccess.isCategorizeBookings() || bankAccess.isStoreAnalytics()) {
        	// create analytics
        	// identify and store contracts
        	analyticsService.startAccountAnalytics(bankAccount.getBankAccessId(), bankAccount.getId());
        }
        if(bankAccess.isStoreAnonymizedBookings()){
        	// anonymize and store booking
        	anonymizationService.anonymizeAndStoreBookingsAsync(bankAccess.getId(), bankAccount.getId());
        }
    }

    private LoadBookingsResponse loadBookingsOnline(BankApi bankApi, BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin) {
        BankApiUser bankApiUser = uds.checkApiRegistration(bankApi, bankAccess.getBankCode());

        OnlineBankingService onlineBankingService = checkAndGetOnlineBankingService(bankAccess, bankAccount, pin, bankApiUser);

        String mappedBlz = bankService.findByBankCode(bankAccess.getBankCode())
                .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode())).getBlzHbci();
        
        try {
            LoadBookingsResponse response = onlineBankingService.loadBookings(bankApiUser, bankAccess, mappedBlz, bankAccount, pin);
            response.setOnlineBankingService(onlineBankingService);
            return response;
        } catch (InvalidPinException e) {
        	credentialService.setInvalidPin(bankAccess.getId());
            throw new de.adorsys.multibanking.exception.InvalidPinException(bankAccess.getId());
        }
    }

	private List<BookingEntity> mergeBookings(List<BookingEntity> dbBookings, List<BookingEntity> newBookings) {
        dbBookings.addAll(newBookings);

        return dbBookings
                .stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
    }

    private OnlineBankingService checkAndGetOnlineBankingService(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin, BankApiUser bankApiUser) {
        OnlineBankingService onlineBankingService = bankingServiceProducer.getBankingService(bankApiUser.getBankApi());

        //external (figo, finapi) account must exist, otherwise loading bookings will not work
        if (onlineBankingService.externalBankAccountRequired()) {
            checkExternalBankAccountExists(bankAccess, bankAccount, pin, bankApiUser, onlineBankingService);
        }

        return onlineBankingService;
    }

    private void checkExternalBankAccountExists(BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin, BankApiUser bankApiUser, OnlineBankingService onlineBankingService) {
        UserData userData = uds.load();
    	
        String externalAccountId = bankAccount.getExternalIdMap().get(onlineBankingService.bankApi());
        //account not created by given bank-api, account must be created, otherwise loading bookings will not work
        if (externalAccountId == null) {
            String blzHbci = bankService.findByBankCode(bankAccess.getBankCode())
                    .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode())).getBlzHbci();
            List<BankAccount> apiBankAccounts = onlineBankingService.loadBankAccounts(bankApiUser, bankAccess, blzHbci, pin, bankAccess.isStorePin());
            BankAccessData bankAccessData = userData.bankAccessData(bankAccess.getId());
	        Collection<BankAccountData> dbBankAccounts = bankAccessData.getBankAccounts().values();
            apiBankAccounts.forEach(apiBankAccount -> {
                dbBankAccounts.forEach(dbBankAccountData -> {
                	BankAccountEntity dbBankAccount = dbBankAccountData.getBankAccount();
                    if (apiBankAccount.getAccountNumber().equals(dbBankAccount.getAccountNumber())) {
                        dbBankAccount.externalId(onlineBankingService.bankApi(), apiBankAccount.getExternalIdMap().get(onlineBankingService.bankApi()));
                        if (bankAccess.getId().equals(dbBankAccount.getId())) {
                            bankAccess.externalId(onlineBankingService.bankApi(), apiBankAccount.getExternalIdMap().get(onlineBankingService.bankApi()));
                        }
                    }
                });
            });
            uds.store(userData);
        }
        
    }

	private static TypeReference<List<BookingEntity>> listType(){
		return new TypeReference<List<BookingEntity>>() {};
	}	
}
