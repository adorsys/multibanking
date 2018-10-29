package de.adorsys.multibanking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.UnexistentBookingFileException;
import de.adorsys.multibanking.service.analytics.AnalyticsService;
import de.adorsys.multibanking.service.analytics.AnonymizationService;
import de.adorsys.multibanking.service.analytics.SmartAnalyticsService;
import de.adorsys.multibanking.service.analytics.SmartanalyticsMapper;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.helper.BookingHelper;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.FQNUtils;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import domain.*;
import domain.request.LoadAccountInformationRequest;
import domain.request.LoadBookingsRequest;
import domain.response.LoadBookingsResponse;
import exception.InvalidPinException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spi.OnlineBankingService;
import utils.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author fpo 2018-03-17 12:16
 */
@Service
public class BookingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(BookingService.class);

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
    private SmartAnalyticsService smartAnalyticsService;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private BankService bankService;
    @Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private AnonymizationService anonymizationService;
    @Autowired
    private DocumentSafeService documentSafeService;

    private static TypeReference<List<BookingEntity>> listType() {
        return new TypeReference<List<BookingEntity>>() {
        };
    }

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
        BankAccountData bankAccountData = uds.load().bankAccountDataOrException(accessId, accountId);
        DocumentFQN bookingFQN = FQNUtils.bookingFQN(accessId, accountId, period);
        if (!bankAccountData.containsBookingFileOfPeriod(period))
            throw new UnexistentBookingFileException(bookingFQN.getValue());
        return documentSafeService.readDocument(uos.auth(), bookingFQN);
    }

    public List<BookingEntity> getAllBookingsAlList(String accessId, String accountId) {
        return loadAllBookings(uds.load(), accessId, accountId);
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
        UserData userData = uds.load();
        BankAccountData bankAccountData = userData.bankAccountDataOrException(accessId, accountId);
        // Set the synch status and flush
        bankAccountData.updateSyncStatus(BankAccount.SyncStatus.SYNC);
        uos.flush();

        // Reload
        BankAccessEntity bankAccess = userData.bankAccessDataOrException(accessId).getBankAccess();
        BankAccountEntity bankAccount = bankAccountData.getBankAccount();
        LoadBookingsResponse response = loadBookingsOnline(bankApi, bankAccess, bankAccount, pin);

        bankAccount.setBalances(response.getBankAccountBalance());
        if (bankAccess.isStoreBookings()) {
            bankAccount.setLastSync(LocalDateTime.now());
            bankAccountService.saveBankAccount(bankAccount);
        }

        if (!bankAccess.isTemporary()) {
            //update bankaccess, passportstate changed
            bankAccessService.updateBankAccess(bankAccess);
        }

        processBookings(userData, bankAccess, bankAccount, response);
    }

    private void processBookings(UserData userData, BankAccessEntity bankAccess,
                                 BankAccountEntity bankAccount, LoadBookingsResponse response) {

        AccountSynchPref accountSynchPref = bankAccountService.findAccountSynchPref(bankAccess.getId(), bankAccount.getId());

        // Booking downloaded from the online banking system
        Map<String, List<BookingEntity>> bookings = BookingHelper.mapBookings(bankAccount, accountSynchPref, response.getBookings());

        // Processed booking periods. Used for anonymization
        // Check with alex if we can use this for analytics.
        // I don't think we need to reload all bookings of a user for analytics.
        BankAccountData bankAccountData = userData.bankAccountDataOrException(bankAccess.getId(), bankAccount.getId());
        Map<String, List<BookingEntity>> processBookingPeriods = storeBookings(bankAccountData, response.getStandingOrders(), bankAccess.isStoreBookings(), bookings);

        bankAccountService.saveStandingOrders(bankAccount, response.getStandingOrders());
        bankAccountData.updateSyncStatus(BankAccount.SyncStatus.READY);
        uos.flush();

        if (bankAccess.isCategorizeBookings() || bankAccess.isStoreAnalytics()) {
            bankAccountData = userData.bankAccountDataOrException(bankAccess.getId(), bankAccount.getId());
            List<BookingEntity> bookingEntities = loadAllBookings(userData, bankAccess.getId(), bankAccount.getId());
            LocalDate analyticsDate = LocalDate.now();
            // TODO. I don't like this smartanalytic that takes all booking.
            // Check for an API for incremental loading of bookings.
            AnalyticsResult analyticsResult = smartAnalyticsService.analyzeBookings(bankAccess.getUserId(), bookingEntities);
            if (analyticsResult != null) {
                if (!response.getOnlineBankingService().bookingsCategorized()) {
                    SmartanalyticsMapper.applyCategories(bookingEntities, analyticsResult);
                }
                if (bankAccess.isStoreAnalytics()) {
                    Map<String, List<BookingEntity>> mapBookings = BookingHelper.reMapBookings(bookingEntities);
                    processBookingPeriods = storeBookings(bankAccountData, response.getStandingOrders(), bankAccess.isStoreBookings(), bookings);
                    analyticsService.saveAccountAnalytics(bankAccount, analyticsResult, analyticsDate);
                    analyticsService.identifyAndStoreContracts(bankAccount.getUserId(), bankAccount.getId(), analyticsResult);
                }
                // Anonymize and store in user space.
                // We will think about releasing this to system later.
                if (bankAccess.isStoreAnalytics() && bankAccess.isStoreAnonymizedBookings()) {
                    Set<Entry<String, List<BookingEntity>>> entries = processBookingPeriods.entrySet();
                    entries.forEach(entry -> {
                        String period = entry.getKey();
                        List<AnonymizedBookingEntity> anonymizedBookings = anonymizationService.anonymizeAndStoreBookingsAsync(entry.getValue());
                        DocumentFQN anonymizedBookingsFQN = FQNUtils.anonymizedBookingFQN(bankAccess.getId(), bankAccount.getId(), period);
                        uos.store(anonymizedBookingsFQN, anonymizedBookingsListType(), anonymizedBookings);
                    });
                }
            }
        }
    }

    private List<BookingEntity> loadAllBookings(UserData userData, String accessId, String accountId) {
        BankAccountData bankAccountData = userData.bankAccountDataOrException(accessId, accountId);
        List<BookingFile> bookingFiles = bankAccountData.getBookingFiles();
        List<BookingEntity> result = new ArrayList<>();
        bookingFiles.forEach(bookingFile -> {
            String period = bookingFile.getPeriod();
            if (bookingFile.getNumberOfRecords() > 0) {
                DocumentFQN bookingFQN = FQNUtils.bookingFQN(accessId, accountId, period);
                List<BookingEntity> existingBookings = uos.load(bookingFQN, listType()).orElse(new ArrayList<>());
                result.addAll(existingBookings);
            }
        });
        return result;
    }

    private LoadBookingsResponse loadBookingsOnline(BankApi bankApi, BankAccessEntity bankAccess, BankAccountEntity bankAccount, String pin) {
        BankApiUser bankApiUser = uds.checkApiRegistration(bankApi, bankAccess);

        OnlineBankingService onlineBankingService = checkAndGetOnlineBankingService(bankAccess, bankAccount, pin, bankApiUser);

        BankEntity bankEntity = bankService.findByBankCode(bankAccess.getBankCode())
                .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode()));

        try {
            LoadBookingsResponse response = onlineBankingService.loadBookings(
                    Optional.ofNullable(bankEntity.getBankingUrl()),
                    LoadBookingsRequest.builder()
                            .bankApiUser(bankApiUser)
                            .bankAccess(bankAccess)
                            .bankCode(bankEntity.getBlzHbci())
                            .bankAccount(bankAccount)
                            .pin(pin)
                            .dateFrom(bankAccount.getLastSync() != null ? bankAccount.getLastSync().toLocalDate() : null)
                            .withTanTransportTypes(true)
                            .withBalance(true)
                            .withStandingOrders(true)
                            .build()
            );
            response.setOnlineBankingService(onlineBankingService);
            return response;
        } catch (InvalidPinException e) {
            try {
                credentialService.setInvalidPin(bankAccess.getId());
            } catch (Exception ex) {
                // Noop
            }
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
            List<BankAccount> apiBankAccounts = onlineBankingService.loadBankAccounts(
                    Optional.empty(),
                    LoadAccountInformationRequest.builder()
                            .bankApiUser(bankApiUser)
                            .bankAccess(bankAccess)
                            .bankCode(blzHbci)
                            .updateTanTransportTypes(true)
                            .pin(pin)
                            .storePin(bankAccess.isStorePin())
                            .build()
            ).getBankAccounts();
            BankAccessData bankAccessData = userData.bankAccessDataOrException(bankAccess.getId());
            List<BankAccountData> dbBankAccounts = bankAccessData.getBankAccounts();
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

    private Map<String, List<BookingEntity>> storeBookings(final BankAccountData bankAccountData, List<StandingOrder> standingOrders, boolean persist, Map<String, List<BookingEntity>> bookings) {
        Map<String, List<BookingEntity>> processBookingPeriods = new HashMap<>();
        String accessId = bankAccountData.getBankAccount().getBankAccessId();
        String accountId = bankAccountData.getBankAccount().getId();
        List<BookingFile> bookingFiles = bankAccountData.getBookingFiles();
        Set<Entry<String, List<BookingEntity>>> entrySet = bookings.entrySet();
        for (Entry<String, List<BookingEntity>> entry : entrySet) {
            List<BookingEntity> bookingEntities = entry.getValue();
            // Process standing orders
            bookingEntities.forEach(booking ->
                    standingOrders
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

            // Merge booking files per period
            String period = entry.getKey();
            DocumentFQN bookingFQN = FQNUtils.bookingFQN(accessId, accountId, period);
            List<BookingEntity> existingBookings = uos.load(bookingFQN, listType())
                    .orElse(new ArrayList<>());
            bookingEntities = mergeBookings(existingBookings, bookingEntities);

            // Store meta data
            Optional<BookingFile> bookingFile = bankAccountData.findBookingFileOfPeriod(period);

            if (!bookingFile.isPresent()) {
                bookingFile = Optional.of(new BookingFile());
                bookingFile.get().setPeriod(period);
                bookingFile.get().setLastUpdate(LocalDateTime.now());
                bankAccountData.update(Collections.singletonList(bookingFile.get()));
            }
            bookingFile.get().setNumberOfRecords(bookingEntities.size());

            // Sort and store bookings
            Collections.sort(bookingEntities, (o1, o2) -> o2.getBookingDate().compareTo(o1.getBookingDate()));
            processBookingPeriods.put(period, bookingEntities);
            if (persist) {
                bookingEntities.forEach(b -> {
                    if (StringUtils.isBlank(b.getId())) b.setId(Ids.uuid());
                });
                uos.store(bookingFQN, listType(), bookingEntities);
            }
        }
        return processBookingPeriods;

    }

    private TypeReference<List<AnonymizedBookingEntity>> anonymizedBookingsListType() {
        return new TypeReference<List<AnonymizedBookingEntity>>() {
        };
    }
}
