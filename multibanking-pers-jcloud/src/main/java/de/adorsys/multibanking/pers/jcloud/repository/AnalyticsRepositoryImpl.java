package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.Optional;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.pers.jcloud.domain.UserMainRecord;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.pers.utils.ObjectPersistenceAdapter;
import de.adorsys.multibanking.pers.utils.UserDataNamingPolicy;

@Service
public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIf {

    @Autowired
    private KeyCredentials keyCredentials;
    
    @Autowired
    private UserDataNamingPolicy namingPolicy;

    @Autowired
    private ObjectPersistenceAdapter objectPersistenceAdapter;

    /* We assume one account has one analytic.*/
	@Override
	public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		AccountAnalyticsEntity accountAnalyticsEntity = userMainRecord.getAnalytics().get(bankAccountId);
		if(accountAnalyticsEntity==null) return Optional.empty();
		return Optional.of(accountAnalyticsEntity);
	}

	@Override
	public void save(AccountAnalyticsEntity accountAnalyticsEntity) {
		ObjectHandle userMainRecordhandle = namingPolicy.handleForUserMainRecord(keyCredentials);
		UserMainRecord userMainRecord = objectPersistenceAdapter.load(userMainRecordhandle, UserMainRecord.class, keyCredentials);
		accountAnalyticsEntity.setId(accountAnalyticsEntity.getAccountId());
		userMainRecord.getAnalytics().put(accountAnalyticsEntity.getAccountId(), accountAnalyticsEntity);
	}

}
