package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.ContainerPersistence;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserMainRecord;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.pers.utils.ObjectPersistenceAdapter;
import de.adorsys.multibanking.pers.utils.UserDataNamingPolicy;

@Service
public class UserRepositoryImpl implements UserRepositoryIf, BankAccessRepositoryIf {

    @Autowired
    private KeyCredentials keyCredentials;
    
    @Autowired
    private UserDataNamingPolicy namingPolicy;
    
    @Autowired
    private ContainerPersistence containerPersistence;
    
    @Autowired
    private ObjectPersistenceAdapter objectPersistenceAdapter;

    
	@Override
	public Optional<UserEntity> findById(String id) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null || userMainRecord.getUserEntity()==null) return Optional.empty();
		return Optional.of(userMainRecord.getUserEntity());
	}

    @Override
    public List<UserEntity> findExpiredUser() {
        return null;
    }

    @Override
	public boolean exists(String userId) {
		String userContainer = namingPolicy.nameUserContainer(userId);
		return containerPersistence.containerExists(userContainer);
	}

	@Override
	public boolean deleteByUserIdAndBankAccessId(String userId, String bankAccessId) {
		//TODO
		return false;
	}

	@Override
	public BankAccessEntity findOne(String id) {
		//TODO
		return null;
	}

	@Override
	public void save(UserEntity userEntity) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null){
			userMainRecord = new UserMainRecord();
		}
		if(userMainRecord.getUserEntity()!=null){
			userEntity.setId(userMainRecord.getUserEntity().getId());
		}
		if(userEntity.getId()==null){
			userEntity.setId(UUID.randomUUID().toString());
		}
		userMainRecord.setUserEntity(userEntity);

		objectPersistenceAdapter.store(userMainRecordhandle, userMainRecord, keyCredentials);
	}

	@Override
	public Optional<BankAccessEntity> findByUserIdAndId(String userId, String id) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null || userMainRecord.getBankAccesses()==null) return Optional.empty();
		List<BankAccessEntity> bankAccesses = userMainRecord.getBankAccesses();
		for (BankAccessEntity bankAccessEntity : bankAccesses) {
			if(StringUtils.equals(id, bankAccessEntity.getId())) return Optional.of(bankAccessEntity);
		}
		return Optional.empty();
	}

	@Override
	public List<BankAccessEntity> findByUserId(String userId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null || userMainRecord.getBankAccesses()==null) return Collections.emptyList();
		return userMainRecord.getBankAccesses();
	}

	@Override
	public BankAccessEntity save(BankAccessEntity bankAccess) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		if(userMainRecord==null)userMainRecord = new UserMainRecord();
		if(userMainRecord.getBankAccesses()==null) userMainRecord.setBankAccesses(new ArrayList<>());
		if(bankAccess.getId()==null)bankAccess.setId(UUID.randomUUID().toString());
		List<BankAccessEntity> bankAccesses = userMainRecord.getBankAccesses();
		int index = -1;
		for (int i = 0; i < bankAccesses.size(); i++) {
			BankAccessEntity bankAccessEntity = bankAccesses.get(i);
			if(bankAccessEntity==null) continue;
			if(StringUtils.equals(bankAccessEntity.getId(), bankAccess.getId())) {
				index = i;
				break;
			}
		}
		if(index<0){
			bankAccesses.add(bankAccess);
		} else {
			if(bankAccess.getId()==null){
				bankAccess.setId(UUID.randomUUID().toString());
			}
			bankAccesses.set(index, bankAccess);
		}
		objectPersistenceAdapter.store(userMainRecordhandle, userMainRecord, keyCredentials);
		return bankAccess;
	}

	@Override
	public String getBankCode(String bankAccessId) {
		return null;
	}

}
