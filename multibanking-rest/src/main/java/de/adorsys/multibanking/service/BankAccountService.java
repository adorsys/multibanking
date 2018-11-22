package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.Ids;
import domain.*;
import domain.request.LoadAccountInformationRequest;
import exception.InvalidPinException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spi.OnlineBankingService;

import java.util.*;

@Service
public class BankAccountService {
    private static final Logger log = LoggerFactory.getLogger(BankAccessService.class);

    @Autowired
    private UserDataService uds;

    @Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private BankService bankService;

    public void synchBankAccounts(BankAccessEntity bankAccess, BankAccessCredentials credentials) {
        List<BankAccountEntity> bankAccounts = loadFromBankingAPI(bankAccess, credentials, null);

        if (bankAccounts.size() == 0) {
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }
        UserData userData = uds.load();
        BankAccessData bankAccessData = userData.bankAccessDataOrException(bankAccess.getId());
        //Map<String, BankAccountData> bankAccountDataMap = bankAccessData.getBankAccounts();
        List<BankAccountData> bankAccountData = bankAccessData.getBankAccounts();
        bankAccounts.forEach(account -> {
            account.bankAccessId(bankAccess.getId());
            Optional<BankAccountData> accountData = bankAccessData.getBankAccount(account.getId());
            if (!accountData.isPresent()) {
                accountData = Optional.of(new BankAccountData());
                bankAccountData.add(accountData.get());
            }
            accountData.get().setBankAccount(account);
        });
        uds.store(userData);
        log.info("[{}] accounts for connection [{}] created.", bankAccounts.size(), bankAccess.getId());
    }

    public List<BankAccountEntity> loadFromBankingAPI(BankAccessEntity bankAccess, BankAccessCredentials credentials, BankApi bankApi) {
        OnlineBankingService onlineBankingService = bankApi != null
                ? bankingServiceProducer.getBankingService(bankApi)
                : bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (!onlineBankingService.bankSupported(bankAccess.getBankCode())) {
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }

        BankApiUser bankApiUser = uds.checkApiRegistration(bankApi, bankAccess);
        String blzHbci = bankService.findByBankCode(bankAccess.getBankCode())
                .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode())).getBlzHbci();

        List<BankAccount> bankAccounts;
        try {
            bankAccounts = Optional.ofNullable(onlineBankingService.loadBankAccounts(
                    Optional.empty(),
                    LoadAccountInformationRequest.builder()
                    .bankApiUser(bankApiUser)
                    .bankAccess(bankAccess)
                    .bankCode(blzHbci)
                    .pin(credentials.getPin())
                    .storePin(false)
                    .updateTanTransportTypes(true)
                    .build()
            )).map(ba -> ba.getBankAccounts()).orElse(Collections.EMPTY_LIST);
        } catch (InvalidPinException e) {
            throw new de.adorsys.multibanking.exception.InvalidPinException(bankAccess.getId());
        }

        if (onlineBankingService.bankApi() == BankApi.FIGO) {
            filterAccounts(bankAccess, onlineBankingService, bankAccounts);
        }

        List<BankAccountEntity> bankAccountEntities = new ArrayList<>();

        bankAccounts.forEach(source -> {
            BankAccountEntity target = new BankAccountEntity();
            target.id(source.getIban());
            BeanUtils.copyProperties(source, target);
            target.setUserId(bankAccess.getUserId());
            bankAccountEntities.add(target);
        });

        return bankAccountEntities;
    }

    public BankAccountData loadBankAccount(String accessId, String accountId) {
        UserData userData = uds.load();
        return userData.bankAccountDataOrException(accessId, accountId);
    }

    /**
     * Saves an existing bank account. Will not add the bank account if absent.
     * Adding a bank account only occurs thru synch.
     *
     * @param in
     */
    public void saveBankAccount(BankAccountEntity in) {
        UserData userData = uds.load();
        userData.bankAccountDataOrException(in.getBankAccessId(), in.getId()).setBankAccount(in);
        uds.store(userData);
    }

    public void saveBankAccounts(String accessId, List<BankAccountEntity> accounts) {
        UserData userData = uds.load();
        for (BankAccountEntity in : accounts) {
            userData.bankAccountDataOrException(in.getBankAccessId(), in.getId()).setBankAccount(in);
        }
        uds.store(userData);
    }

    public boolean exists(String accessId, String accountId) {
        UserData userData = uds.load();
        return userData.bankAccessDataOrException(accessId).containsKey(accountId);
    }

//	public SyncStatus getSyncStatus(String accessId, String accountId) {
//		UserData userData = uds.load();
//		return userData.bankAccountData(accessId, accountId).getBankAccount().getSyncStatus();
//	}

    public AccountSynchPref loadAccountLevelSynchPref(String accessId, String accountId) {
        return uds.load().bankAccountDataOrException(accessId, accountId).getAccountSynchPref();
    }

    public void storeAccountLevelSynchPref(String accessId, String accountId, AccountSynchPref pref) {
        UserData userData = uds.load();
        userData.bankAccountDataOrException(accessId, accountId).setAccountSynchPref(pref);
        uds.store(userData);
    }

    public AccountSynchPref loadAccessLevelSynchPref(String accessId) {
        return uds.load().bankAccessDataOrException(accessId).getAccountSynchPref();
    }

    public void storeAccessLevelSynchPref(String accessId, AccountSynchPref pref) {
        UserData userData = uds.load();
        userData.bankAccessDataOrException(accessId).setAccountSynchPref(pref);
        uds.store(userData);
    }

    public AccountSynchPref loadUserLevelSynchPref() {
        return uds.load().getAccountSynchPref();
    }

    public void storeUserLevelSynchPref(AccountSynchPref pref) {
        UserData userData = uds.load();
        userData.setAccountSynchPref(pref);
        uds.store(userData);
    }

//	public AccountSynchResult loadAccountSynchResult(String accessId, String accountId) {
//		return uds.load().bankAccountData(accessId, accountId).getSynchResult();
//	}
//	public void storeAccountSynchResult(String accessId, String accountId, AccountSynchResult currentResult) {
//		UserData userData = uds.load();
//		userData.bankAccountData(accessId, accountId).setSynchResult(currentResult);
//		uds.store(userData);
//	}

    /**
     * Search the neares account synch preference for the given account
     *
     * @param accessId
     * @param accountId
     * @return
     */
    public AccountSynchPref findAccountSynchPref(String accessId, String accountId) {
        AccountSynchPref synchPref = loadAccountLevelSynchPref(accessId, accountId);
        if (synchPref == null)
            synchPref = loadAccessLevelSynchPref(accessId);
        if (synchPref == null)
            synchPref = loadUserLevelSynchPref();
        if (synchPref == null)
            synchPref = new AccountSynchPref();

        return synchPref;
    }

    /**
     * Store standing orders in the user data record. Uses the delivered orderId to identify
     * existing records and exchange them.
     *
     * @param bankAccount
     * @param standingOrders
     */
    public void saveStandingOrders(BankAccountEntity bankAccount, List<StandingOrder> standingOrders) {
        UserData userData = uds.load();
        Map<String, StandingOrderEntity> standingOrdersMap = userData.bankAccountDataOrException(bankAccount.getBankAccessId(), bankAccount.getId()).getStandingOrders();
        standingOrders.stream()
                .map(standingOrder -> {
                    // Assign an order id if none.
                    if (StringUtils.isBlank(standingOrder.getOrderId())) {
                        standingOrder.setOrderId(Ids.uuid());
                    }

                    // Check existence of this standing order in the user data record.
                    // Instantiate and add one if none.
                    StandingOrderEntity target = standingOrdersMap.get(standingOrder.getOrderId());
                    if (target == null) {
                        target = new StandingOrderEntity();
                        Ids.id(target);
                        standingOrdersMap.put(standingOrder.getOrderId(), target);
                        target.setAccountId(bankAccount.getId());
                        target.setUserId(bankAccount.getUserId());
                    }

                    // Update the record.
                    BeanUtils.copyProperties(standingOrder, target);
                    return target;
                });
        uds.store(userData);
    }

    /**
     * Replace all contracts associated with the given bank account.
     * <p>
     * We assume to contracts given are associated with the bank account.
     */
    public void saveContracts(String accountId, List<ContractEntity> contractEntities) {
        UserData userData = uds.load();
        BankAccountData bankAccountData = findBankAccountData(userData, accountId);
        if (bankAccountData == null) throw new ResourceNotFoundException(BankAccountData.class, accountId);
        // Set ids.
        BankAccountEntity bankAccount = bankAccountData.getBankAccount();
        contractEntities.stream().forEach(c -> {
            c.setAccessId(bankAccount.getBankAccessId());
            c.setUserId(bankAccount.getUserId());
            c.setAccountId(bankAccount.getId());
        });

        bankAccountData.setContracts(new ArrayList<>(contractEntities));
        uds.store(userData);
    }

    public void saveAccountAnalytics(String accountId, AccountAnalyticsEntity analytic) {
        UserData userData = uds.load();
        BankAccountData bankAccountData = findBankAccountData(userData, accountId);
        if (bankAccountData == null) throw new ResourceNotFoundException(BankAccountData.class, accountId);
        BankAccountEntity bankAccount = bankAccountData.getBankAccount();
        analytic.setUserId(bankAccount.getUserId());
        analytic.setAccountId(bankAccount.getId());
        bankAccountData.setAnalytic(analytic);
        uds.store(userData);
    }

    private BankAccountData findBankAccountData(UserData userData, String accountId) {
        List<BankAccessData> bankAccesses = userData.getBankAccesses();
        for (BankAccessData b : bankAccesses) {
            Optional<BankAccountData> bankAccount = b.getBankAccount(accountId);
            if (bankAccount.isPresent()) return bankAccount.get();
        }
        return null;
    }

    private void filterAccounts(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService, List<BankAccount> bankAccounts) {
        UserData userData = uds.load();
        List<BankAccountData> userBankAccounts = userData.bankAccessDataOrException(bankAccess.getId()).getBankAccounts();
//        List<BankAccountEntity> userBankAccounts = loadForBankAccess(bankAccess.getId());

        //filter out previous created accounts
        Iterator<BankAccount> accountIterator = bankAccounts.iterator();
        while (accountIterator.hasNext()) {
            BankAccount newAccount = accountIterator.next();
            userBankAccounts.stream().filter(bankAccountData -> {
                String newAccountExternalID = newAccount.getExternalIdMap().get(onlineBankingService.bankApi());
                String existingAccountExternalID = bankAccountData.getBankAccount().getExternalIdMap().get(onlineBankingService.bankApi());

                return newAccountExternalID.equals(existingAccountExternalID);
            }).findFirst().ifPresent(bankAccountEntity -> {
                accountIterator.remove();
            });
        }

        //all accounts created in the past
        if (bankAccounts.size() == 0) {
            throw new BankAccessAlreadyExistException(bankAccess.getId());
        }

        bankAccess.setBankName(bankAccounts.get(0).getBankName());
    }

}
