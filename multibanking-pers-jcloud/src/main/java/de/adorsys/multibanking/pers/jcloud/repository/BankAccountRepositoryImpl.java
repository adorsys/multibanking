package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import domain.BankAccount;
import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserMainRecord;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.utils.ObjectPersistenceAdapter;
import de.adorsys.multibanking.pers.utils.UserDataNamingPolicy;

@Service
public class BankAccountRepositoryImpl implements BankAccountRepositoryIf {

    @Autowired
    private KeyCredentials keyCredentials;
    
    @Autowired
    private UserDataNamingPolicy namingPolicy;

    @Autowired
    private ObjectPersistenceAdapter objectPersistenceAdapter;

	@Override
	public List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null || userMainRecord.getBankAccountMap()==null) return Collections.emptyList();
		
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		return bankAccountMap.get(bankAccessId);
	}

	@Override
	public Optional<BankAccountEntity> findByUserIdAndId(String userId, String id) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null || userMainRecord.getBankAccountMap()==null) return Optional.empty();
		
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		Collection<List<BankAccountEntity>> values = bankAccountMap.values();
		for (List<BankAccountEntity> list : values) {
			if(list==null) continue;
			for (BankAccountEntity bankAccountEntity : list) {
				if(bankAccountEntity==null) continue;
				if(StringUtils.equals(id, bankAccountEntity.getId())) return Optional.of(bankAccountEntity);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean exists(String accountId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null || userMainRecord.getBankAccountMap()==null) return false;
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		Collection<List<BankAccountEntity>> values = bankAccountMap.values();
		for (List<BankAccountEntity> list : values) {
			if(list==null) continue;
			for (BankAccountEntity bankAccountEntity : list) {
				if(bankAccountEntity==null) continue;
				if(StringUtils.equals(accountId, bankAccountEntity.getId())) return true;
			}
		}
		return false;
	}

	@Override
	public void save(List<BankAccountEntity> bankAccounts) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null)userMainRecord = new UserMainRecord();
		if(userMainRecord.getBankAccountMap()==null)userMainRecord.setBankAccountMap(new HashMap<>());
		
		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		for (BankAccountEntity bankAccountEntity : bankAccounts) {
			add(bankAccountMap, bankAccountEntity);
		}
	}

	@Override
	public void save(BankAccountEntity bankAccount) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null)userMainRecord = new UserMainRecord();
		if(userMainRecord.getBankAccountMap()==null)userMainRecord.setBankAccountMap(new HashMap<>());

		Map<String, List<BankAccountEntity>> bankAccountMap = userMainRecord.getBankAccountMap();
		add(bankAccountMap, bankAccount);
	}

	@Override
	public BankAccount.SyncStatus getSyncStatus(String accountId) {
		return null;
	}

	@Override
	public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {

	}

	private void add(Map<String, List<BankAccountEntity>> bankAccountMap, BankAccountEntity bankAccount){
		String bankAccessId = bankAccount.getBankAccessId();
		List<BankAccountEntity> list = bankAccountMap.get(bankAccessId);
		if(list==null){
			list = new ArrayList<>();
			bankAccountMap.put(bankAccessId, list);
		}
		
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			BankAccountEntity bankAccessEntity = list.get(i);
			if(bankAccessEntity==null) continue;
			if(StringUtils.equals(bankAccessEntity.getId(), bankAccount.getId())) {
				index = i;
				break;
			}
		}
		if(index<0){
			list.add(bankAccount);
		} else {
			if(bankAccount.getId()==null){
				bankAccount.setId(UUID.randomUUID().toString());
			}
			list.set(index, bankAccount);
		}
		
	}

}
