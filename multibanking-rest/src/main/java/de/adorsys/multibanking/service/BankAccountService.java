package de.adorsys.multibanking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.AccountSynchPref;
import de.adorsys.multibanking.domain.AccountSynchResult;
import de.adorsys.multibanking.domain.BankAccessCredentials;
import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.Ids;
import domain.BankAccount;
import domain.BankAccount.SyncStatus;
import domain.BankApi;
import domain.BankApiUser;
import domain.StandingOrder;
import exception.InvalidPinException;
import spi.OnlineBankingService;

@Service
public class BankAccountService {
    private static final Logger log = LoggerFactory.getLogger(BankAccessService.class);

	@Autowired
    private UserDataService uds;
    
    @Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private BankService bankService;
	
    public void synchBankAccounts(BankAccessEntity bankAccess, BankAccessCredentials credentials){
    	List<BankAccountEntity> bankAccounts = loadFromBankingAPI(bankAccess, credentials, null);
        
        if (bankAccounts.size() == 0) {
            throw new InvalidBankAccessException(bankAccess.getBankCode());
        }
        UserData userData = uds.load();
        BankAccessData bankAccessData = userData.bankAccessData(bankAccess.getId());
        Map<String, BankAccountData> bankAccountDataMap = bankAccessData.getBankAccounts();
        bankAccounts.forEach(account -> {
        	account.bankAccessId(bankAccess.getId());
        	BankAccountData accountData = bankAccountDataMap.get(account.getId());
        	if(accountData==null){
        		accountData = new BankAccountData();
        		bankAccountDataMap.put(account.getId(), accountData);
        	}
        	accountData.setBankAccount(account);
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

        BankApiUser bankApiUser = uds.checkApiRegistration(bankApi, bankAccess.getBankCode());
        String blzHbci = bankService.findByBankCode(bankAccess.getBankCode())
                .orElseThrow(() -> new ResourceNotFoundException(BankEntity.class, bankAccess.getBankCode())).getBlzHbci();

        List<BankAccount> bankAccounts;
        try {
            bankAccounts = onlineBankingService.loadBankAccounts(bankApiUser, bankAccess, blzHbci, credentials.getPin(), false);
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

	public void updateSyncStatus(String accessId, String accountId, SyncStatus syncStatus) {
		UserData userData = uds.load();
		userData.bankAccountData(accessId, accountId).getBankAccount().setSyncStatus(syncStatus);
		AccountSynchResult synchResult = userData.bankAccountData(accessId, accountId).getSynchResult();
		synchResult.setSyncStatus(syncStatus);
		synchResult.setStatusTime(LocalDateTime.now());
		uds.store(userData);
	}
	
	/**
	 * Saves an existing bank account. Will not add the bank account if absent.
	 * Adding a bank account only occurs thru synch.
	 * 
	 * @param in
	 */
	public void saveBankAccount(BankAccountEntity in){
		UserData userData = uds.load();
		userData.bankAccountData(in.getBankAccessId(), in.getId()).setBankAccount(in);
		uds.store(userData);
	}

	public void saveBankAccounts(String accessId, List<BankAccountEntity> accounts){
		UserData userData = uds.load();
		for (BankAccountEntity in : accounts) {
			userData.bankAccountData(in.getBankAccessId(), in.getId()).setBankAccount(in);
		}
		uds.store(userData);
	}
	
	public boolean exists(String accessId, String accountId) {
		UserData userData = uds.load();
		return userData.bankAccessData(accessId).getBankAccounts().containsKey(accountId);
	}
	
	public SyncStatus getSyncStatus(String accessId, String accountId) {
		UserData userData = uds.load();
		return userData.bankAccountData(accessId, accountId).getBankAccount().getSyncStatus();
	}
    
	public AccountSynchPref loadAccountLevelSynchPref(String accessId, String accountId){
		return uds.load().bankAccountData(accessId, accountId).getAccountSynchPref();
	}	
	public void storeAccountLevelSynchPref(String accessId, String accountId, AccountSynchPref pref){
		UserData userData = uds.load();
		userData.bankAccountData(accessId, accountId).setAccountSynchPref(pref);
		uds.store(userData);
	}

	public AccountSynchPref loadAccessLevelSynchPref(String accessId){
		return uds.load().bankAccessData(accessId).getAccountSynchPref();
	}	
	public void storeAccessLevelSynchPref(String accessId, AccountSynchPref pref){
		UserData userData = uds.load();
		userData.bankAccessData(accessId).setAccountSynchPref(pref);
		uds.store(userData);
	}

	public AccountSynchPref loadUserLevelSynchPref(){
		return uds.load().getAccountSynchPref();
	}	
	public void storeUserLevelSynchPref(AccountSynchPref pref){
		UserData userData = uds.load();
		userData.setAccountSynchPref(pref);
		uds.store(userData);
	}
	
	public AccountSynchResult loadAccountSynchResult(String accessId, String accountId) {
		return uds.load().bankAccountData(accessId, accountId).getSynchResult();
	}
	public void storeAccountSynchResult(String accessId, String accountId, AccountSynchResult currentResult) {
		UserData userData = uds.load();
		userData.bankAccountData(accessId, accountId).setSynchResult(currentResult);
		uds.store(userData);
	}
	
	/**
	 * Search the neares account synch preference for the given account
	 * @param id
	 * @param id2
	 * @return 
	 */
	public AccountSynchPref findAccountSynchPref(String accessId, String accountId) {
		AccountSynchPref synchPref = loadAccountLevelSynchPref(accessId, accountId);
		if(synchPref==null) 
			synchPref = loadAccessLevelSynchPref(accessId);
		if(synchPref==null) 
			synchPref = loadUserLevelSynchPref();
		if(synchPref==null) 
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
    	Map<String, StandingOrderEntity> standingOrdersMap = userData.bankAccountData(bankAccount.getBankAccessId(), bankAccount.getId()).getStandingOrders();
        standingOrders.stream()
                .map(standingOrder -> {
                	// Assign an order id if none.
                	if(StringUtils.isBlank(standingOrder.getOrderId())){
                		standingOrder.setOrderId(Ids.uuid());
                	}
                	
                	// Check existence of this standing order in the user data record.
                	// Instantiate and add one if none.
                	StandingOrderEntity target = standingOrdersMap.get(standingOrder.getOrderId());
                	if(target==null){
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
	
	
    private void filterAccounts(BankAccessEntity bankAccess, OnlineBankingService onlineBankingService, List<BankAccount> bankAccounts) {
    	UserData userData = uds.load();
    	Collection<BankAccountData> userBankAccounts = userData.bankAccessData(bankAccess.getId()).getBankAccounts().values();
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
