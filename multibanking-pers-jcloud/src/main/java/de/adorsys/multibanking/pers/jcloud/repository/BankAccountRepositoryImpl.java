package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserMainRecord;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.utils.ObjectPersistenceAdapter;
import de.adorsys.multibanking.pers.utils.UserDataNamingPolicy;
import domain.BankAccount;

@Service
public class BankAccountRepositoryImpl implements BankAccountRepositoryIf {

    @Autowired
    private KeyCredentials keyCredentials;
    
    @Autowired
    private UserDataNamingPolicy namingPolicy;

    @Autowired
    private ObjectPersistenceAdapter objectPersistenceAdapter;

	@Override
	public List<BankAccountEntity> findByUserId(String userId) {
		return null;
	}

	@Override
	public List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null) return Collections.emptyList();
		return userMainRecord.getBankAccountMap().get(bankAccessId);
	}

	@Override
	public Optional<BankAccountEntity> findByUserIdAndId(String userId, String id) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		BankAccountEntity accountEntity = ListUtils.find(userMainRecord, id);
		if(accountEntity==null) return Optional.empty();
		return Optional.of(accountEntity);
	}

	@Override
	public boolean exists(String accountId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		return ListUtils.find(userMainRecord, accountId)!=null;
	}

	@Override
	public void save(List<BankAccountEntity> bankAccounts) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null)userMainRecord = new UserMainRecord();
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		for (BankAccountEntity bankAccountEntity : bankAccounts) {
			add(bankAccountMap, bankAccountEntity);
		}
		objectPersistenceAdapter.store(userMainRecordhandle, userMainRecord, keyCredentials);
	}

	@Override
	public void save(BankAccountEntity bankAccount) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null)userMainRecord = new UserMainRecord();
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		add(bankAccountMap, bankAccount);
		objectPersistenceAdapter.store(userMainRecordhandle, userMainRecord, keyCredentials);
	}

	@Override
	public BankAccount.SyncStatus getSyncStatus(String accountId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		BankAccountEntity accountEntity = ListUtils.find(userMainRecord, accountId);
		if(accountEntity==null) return BankAccount.SyncStatus.READY;
		return accountEntity.getSyncStatus();
	}

	@Override
	public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		BankAccountEntity accountEntity = ListUtils.find(userMainRecord, accountId);
		if(accountEntity==null) throw new RuntimeException( new ObjectNotFoundException("Account with id: " + accountId + " not found"));
		accountEntity.setSyncStatus(syncStatus);
		add(userMainRecord.getBankAccountMap(), accountEntity);
		objectPersistenceAdapter.store(userMainRecordhandle, userMainRecord, keyCredentials);
	}

	@Override
	public List<BankAccountEntity> deleteByBankAccess(String accessId) {
		//TODO
		return null;
	}

	@Override
	public BankAccountEntity findOne(String accountId) {
		//TODO
		return null;
	}

	static ListItemHandler<BankAccountEntity> handler = new ListItemHandler<BankAccountEntity>() {

		@Override
		public boolean idEquals(BankAccountEntity a, BankAccountEntity b) {
			return StringUtils.equals(a.getId(), b.getId());
		}

		@Override
		public boolean newId(BankAccountEntity a) {
			if(StringUtils.isNoneBlank(a.getId())) return false;
			a.setId(UUID.randomUUID().toString());
			return true;
		}
	};
	
	private void add(Map<String, List<BankAccountEntity>> bankAccountMap, BankAccountEntity bankAccount){
		String bankAccessId = bankAccount.getBankAccessId();
		List<BankAccountEntity> list = bankAccountMap.get(bankAccessId);
		if(list==null){
			list = new ArrayList<>();
			bankAccountMap.put(bankAccessId, list);
		}
		
		ListUtils.add(bankAccount, list, handler);
	}

}
