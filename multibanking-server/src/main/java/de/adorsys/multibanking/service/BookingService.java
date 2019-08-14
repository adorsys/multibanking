package de.adorsys.multibanking.service;

import de.adorsys.multibanking.config.FinTSProductConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.StandingOrder;
import de.adorsys.multibanking.domain.utils.Utils;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationException;
import de.adorsys.multibanking.pers.spi.repository.*;
import de.adorsys.multibanking.service.analytics.AnalyticsService;
import de.adorsys.multibanking.service.analytics.SmartAnalyticsIf;
import de.adorsys.multibanking.service.analytics.SmartAnalyticsMapper;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.config.ConfigStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class BookingService extends AccountInformationService {

    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final BookingRepositoryIf bookingRepository;
    private final BookingsIndexRepositoryIf bookingsIndexRepository;
    private final StandingOrderRepositoryIf standingOrderRepository;
    private final AnalyticsRepositoryIf analyticsRepository;
    private final SmartAnalyticsIf smartAnalyticsService;
    private final AnalyticsService analyticsService;
    private final AnonymizationService anonymizationService;
    private final ConsentService consentService;
    private final BankService bankService;
    private final UserService userService;
    private final OnlineBankingServiceProducer bankingServiceProducer;
    private final FinTSProductConfig finTSProductConfig;
    private final SmartAnalyticsMapper smartAnalyticsMapper;

    public String getBookingsCsv(String userId, String accessId, String accountId) {
        List<BookingEntity> bookings = getBookings(userId, accessId, accountId);
        StringBuilder builder = new StringBuilder();

        bookings.forEach(bookingEntity -> {
            builder.append(bookingEntity.getBookingDate() != null ? bookingEntity.getBookingDate().toString() : "");
            builder.append(";");
            builder.append(bookingEntity.getOtherAccount() != null ? bookingEntity.getOtherAccount().getOwner() : "");
            builder.append(";");
            builder.append(bookingEntity.getBookingCategory() != null ?
                bookingEntity.getBookingCategory().getMainCategory() : "");
            builder.append(";");
            builder.append(bookingEntity.getBookingCategory() != null ?
                bookingEntity.getBookingCategory().getSubCategory() : "");
            builder.append(";");
            builder.append(bookingEntity.getBookingCategory() != null ?
                bookingEntity.getBookingCategory().getSpecification() : "");
            builder.append(";");
            builder.append(bookingEntity.getAmount() != null ? bookingEntity.getAmount().toString() : "");
            builder.append(";");
            builder.append(bookingEntity.getCreditorId() != null ? bookingEntity.getCreditorId() : "");
            builder.append(";");
            builder.append(bookingEntity.getUsage() != null ? bookingEntity.getUsage() : "");
            builder.append("\n");
        });
        return builder.toString();
    }

    public Page<BookingEntity> getBookingsPageable(Pageable pageable, String userId, String accessId, String accountId,
                                                   BankApi bankApi) {
        if (bankApi == null) {
            String bankCode = bankAccessRepository.getBankCode(accessId);
            bankApi = bankingServiceProducer.getBankingService(bankCode).bankApi();
        }

        return bookingRepository.findPageableByUserIdAndAccountIdAndBankApi(pageable, userId, accountId, bankApi);
    }

    public Iterable<BookingEntity> getBookingsById(String name, List<String> ids) {
        return bookingRepository.findByUserIdAndIds(name, ids);
    }

    public Optional<BookingsIndexEntity> getSearchIndex(String userId, String accountId) {
        return bookingsIndexRepository.findByUserIdAndAccountId(userId, accountId);
    }

    private List<BookingEntity> getBookings(String userId, String accessId, String accountId) {
        String bankCode = bankAccessRepository.getBankCode(accessId);
        BankApi bankApi = bankingServiceProducer.getBankingService(bankCode).bankApi();

        return bookingRepository.findByUserIdAndAccountIdAndBankApi(userId, accountId, bankApi);
    }

    @Transactional
    public List<BookingEntity> syncBookings(ScaStatus expectedConsentStatus, BankAccessEntity bankAccess,
                                            BankAccountEntity bankAccount, @Nullable BankApi bankApi) {
        bankAccountRepository.updateSyncStatus(bankAccount.getId(), BankAccount.SyncStatus.SYNC);

        OnlineBankingService onlineBankingService = bankApi != null ?
            bankingServiceProducer.getBankingService(bankApi) :
            bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        try {
            LoadBookingsResponse response = loadBookingsOnline(expectedConsentStatus, onlineBankingService,
                bankAccess, bankAccount);

            if (!bankAccess.isTemporary()) {
                //update bankaccess, passportstate changed
                bankAccessRepository.save(bankAccess);
            }

            List<BookingEntity> result = processBookings(onlineBankingService, bankAccess, bankAccount, response);

            Optional.ofNullable(response.getBankAccountBalance())
                .ifPresent(bankAccount::setBalances);

            bankAccount.setSyncStatus(BankAccount.SyncStatus.READY);
            bankAccount.setLastSync(LocalDateTime.now());
            bankAccountRepository.save(bankAccount);

            return result;
        } catch (MissingConsentAuthorisationException e) {
            throw e;
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("sync bookings failed", e);
            throw e;
        } finally {
            bankAccountRepository.updateSyncStatus(bankAccount.getId(), BankAccount.SyncStatus.PENDING);
        }
    }

    private List<BookingEntity> processBookings(OnlineBankingService onlineBankingService, BankAccessEntity bankAccess,
                                                BankAccountEntity bankAccount, LoadBookingsResponse response) {
        List<BookingEntity> newBookings = mapBookings(bankAccount, response.getBookings());
        mapStandingOrders(response, newBookings);

        List<BookingEntity> existingBookings = bookingRepository.findByUserIdAndAccountIdAndBankApi(
            bankAccess.getUserId(), bankAccount.getId(), onlineBankingService.bankApi());

        List<BookingEntity> mergedBookings = mergeBookings(existingBookings, newBookings);

        if (mergedBookings.size() == existingBookings.size() && !rulesVersionChanged(bankAccess.getUserId(),
            bankAccount.getId())) {
            log.info("no bookings or rules changes, skip analytics");
            return existingBookings;
        }

        AnalyticsResult analyticsResult = null;
        if (bankAccess.isCategorizeBookings() || bankAccess.isStoreAnalytics()) {
            analyticsResult = analyticsService.analyzeBookings(bankAccess.getUserId(), mergedBookings);
            if (!onlineBankingService.bookingsCategorized()) {
                smartAnalyticsMapper.applyCategories(mergedBookings, analyticsResult);
            }
        }

        if (bankAccess.isStoreBookings()) {
            bookingRepository.save(mergedBookings);
            saveStandingOrders(bankAccount, response.getStandingOrders());
            updateBookingsIndex(bankAccount, mergedBookings);
        }

        if (bankAccess.isStoreAnonymizedBookings()) {
            anonymizationService.anonymizeAndStoreBookingsAsync(mergedBookings);
        }

        saveAnalytics(analyticsResult, bankAccess, bankAccount, mergedBookings);

        mergedBookings.sort((o1, o2) -> o2.getBookingDate().compareTo(o1.getBookingDate()));

        return mergedBookings;
    }

    private boolean rulesVersionChanged(String userId, String accountId) {
        ConfigStatus analyticsConfigStatus = smartAnalyticsService.getAnalyticsConfigStatus();

        return analyticsRepository.findLastAnalyticsDateByUserIdAndAccountId(userId, accountId)
            .map(lastAnalyticsDate -> {
                if (lastAnalyticsDate.isBefore(analyticsConfigStatus.getLastChangeDate())) {
                    return true;
                }
                return userService.getRulesLastChangeDate(userId)
                    .map(lastAnalyticsDate::isBefore)
                    .orElse(false);
            })
            .orElse(true);

    }

    private void saveAnalytics(AnalyticsResult analyticsResult, BankAccessEntity bankAccess,
                               BankAccountEntity bankAccount, List<BookingEntity> bookingEntities) {
        if (analyticsResult == null) {
            return;
        }

        //period included booking should be mapped with entity db id
        analyticsResult.getBookingGroups()
            .stream()
            .filter(bookingGroup -> bookingGroup.getBookingPeriods() != null)
            .forEach(bookingGroup ->
                bookingGroup.getBookingPeriods().forEach(period ->
                    period.getBookings().forEach(executedBooking -> bookingEntities.stream()
                        .filter(bookingEntity -> bookingEntity.getExternalId().equals(executedBooking.getBookingId()))
                        .findFirst()
                        .ifPresent(bookingEntity -> executedBooking.setBookingId(bookingEntity.getId())))));

        if (bankAccess.isStoreAnalytics()) {
            analyticsService.saveAccountAnalytics(bankAccount, analyticsResult.getBookingGroups());
            analyticsService.identifyAndStoreContracts(bankAccount.getUserId(), bankAccount.getId(),
                analyticsResult.getBookingGroups());
        }
    }

    private void mapStandingOrders(LoadBookingsResponse response, List<BookingEntity> bookingEntities) {
        if (response.getStandingOrders() == null) {
            return;
        }

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
    }

    private void saveStandingOrders(BankAccountEntity bankAccount, List<StandingOrder> standingOrders) {
        Optional.ofNullable(standingOrders)
            .ifPresent(sto -> {
                List<StandingOrderEntity> standingOrderEntities = sto.stream()
                    .map(booking -> {
                        StandingOrderEntity target = new StandingOrderEntity();
                        BeanUtils.copyProperties(booking, target);
                        target.setAccountId(bankAccount.getId());
                        target.setUserId(bankAccount.getUserId());
                        return target;
                    })
                    .collect(Collectors.toList());
                standingOrderRepository.deleteByAccountId(bankAccount.getId());
                standingOrderRepository.save(standingOrderEntities);
            });

    }

    private void updateBookingsIndex(BankAccountEntity bankAccount, List<BookingEntity> bookings) {
        BookingsIndexEntity bookingsIndexEntity =
            bookingsIndexRepository.findByUserIdAndAccountId(bankAccount.getUserId(), bankAccount.getId())
                .orElseGet(() -> {
                    BookingsIndexEntity newIndexEntity = new BookingsIndexEntity();
                    newIndexEntity.setAccountId(bankAccount.getId());
                    newIndexEntity.setUserId(bankAccount.getUserId());
                    return newIndexEntity;
                });

        bookingsIndexEntity.updateSearchIndex(bookings);

        bookingsIndexRepository.save(bookingsIndexEntity);
    }

    private LoadBookingsResponse loadBookingsOnline(ScaStatus expectedConsentStatus,
                                                    OnlineBankingService onlineBankingService,
                                                    BankAccessEntity bankAccess, BankAccountEntity bankAccount) {
        BankApiUser bankApiUser = userService.checkApiRegistration(bankAccess, onlineBankingService.bankApi());

        Optional<ConsentEntity> consentEntity = consentService.validateAndGetConsent(bankAccess, onlineBankingService,
            expectedConsentStatus);
        //external (figo, finapi) account must exist, otherwise loading bookings will not work
        // FIXME this is a problem! currently we load all accounts for bookings which could cause problems with 2FA
        //  in HBCI
        if (onlineBankingService.externalBankAccountRequired()) {
            checkExternalBankAccountExists(bankAccess, bankAccount, bankApiUser,
                onlineBankingService);
        }

        BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

        LoadBookingsRequest loadBookingsRequest = new LoadBookingsRequest();
        loadBookingsRequest.setConsentId(bankAccess.getConsentId());
        loadBookingsRequest.setBankApiUser(bankApiUser);
        loadBookingsRequest.setBankAccess(bankAccess);
        loadBookingsRequest.setBankCode(bankEntity.getBlzHbci());
        loadBookingsRequest.setBankAccount(bankAccount);
        loadBookingsRequest.setPin(bankAccess.getPin());
        loadBookingsRequest.setDateFrom(bankAccount.getLastSync() != null ?
            bankAccount.getLastSync().toLocalDate() : null);
        loadBookingsRequest.setWithTanTransportTypes(true);
        loadBookingsRequest.setWithBalance(true);
        loadBookingsRequest.setWithStandingOrders(true);
        loadBookingsRequest.setBankUrl(bankEntity.getBankingUrl());
        consentEntity.ifPresent(consent -> loadBookingsRequest.setConsentId(consent.getId()));

        try {
            consentEntity.ifPresent(consent -> onlineBankingService.getStrongCustomerAuthorisation().preExecute(loadBookingsRequest,
                consent.getBankApiConsentData())
            );

            return onlineBankingService.loadBookings(loadBookingsRequest);
        } catch (MultibankingException e) {
            throw handleMultibankingException(bankAccess, consentEntity.orElse(null), e);
        }
    }

    private List<BookingEntity> mapBookings(BankAccountEntity bankAccount, List<Booking> bookings) {
        return bookings.stream()
            .map(booking -> {
                BookingEntity target = new BookingEntity();
                BeanUtils.copyProperties(booking, target);
                target.setAccountId(bankAccount.getId());
                target.setUserId(bankAccount.getUserId());
                return target;
            })
            .collect(Collectors.toList());
    }

    private List<BookingEntity> mergeBookings(List<BookingEntity> dbBookings,
                                              List<BookingEntity> newBookings) {
        return Stream.of(dbBookings, newBookings)
            .flatMap(Collection::stream)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(Booking::getExternalId))), ArrayList::new));
    }

    //only for figo
    private void checkExternalBankAccountExists(BankAccessEntity bankAccess,
                                                BankAccountEntity bankAccount, BankApiUser bankApiUser,
                                                OnlineBankingService onlineBankingService) {
        String externalAccountId = bankAccount.getExternalIdMap().get(onlineBankingService.bankApi());
        //account not created by given bank-api, account must be created, otherwise loading bookings will not work
        if (externalAccountId == null) {
            BankEntity bankEntity = bankService.findBank(bankAccess.getBankCode());

            LoadAccountInformationRequest request = new LoadAccountInformationRequest();
            request.setBankUrl(bankEntity.getBankingUrl());
            request.setBankApiUser(bankApiUser);
            request.setBankAccess(bankAccess);
            request.setBankCode(bankEntity.getBlzHbci());
            request.setUpdateTanTransportTypes(true);
            request.setPin(bankAccess.getPin());
            request.setStorePin(bankAccess.isStorePin());
            request.setHbciProduct(finTSProductConfig.getProduct());
            List<BankAccount> apiBankAccounts = onlineBankingService.loadBankAccounts(request).getBankAccounts();

            List<BankAccountEntity> dbBankAccounts = bankAccountRepository
                .findByUserIdAndBankAccessId(bankAccess.getUserId(), bankAccess.getId());

            apiBankAccounts.forEach(apiBankAccount -> dbBankAccounts.forEach(dbBankAccount -> {
                if (apiBankAccount.getAccountNumber().equals(dbBankAccount.getAccountNumber())) {
                    dbBankAccount.externalId(onlineBankingService.bankApi(),
                        apiBankAccount.getExternalIdMap().get(onlineBankingService.bankApi()));
                    bankAccountRepository.save(dbBankAccount);
                    if (bankAccess.getId().equals(dbBankAccount.getId())) {
                        bankAccess.externalId(onlineBankingService.bankApi(),
                            apiBankAccount.getExternalIdMap().get(onlineBankingService.bankApi()));
                    }
                }
            }));
        }
    }

}
