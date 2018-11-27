package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.repository.BankAccountRepositoryMongodb;
import domain.BankAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BankAccountRepositoryImpl implements BankAccountRepositoryIf {

    @Autowired
    private BankAccountRepositoryMongodb bankAccountRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

	@Override
	public List<BankAccountEntity> findByUserId(String userId) {
		return bankAccountRepository.findByUserId(userId);
	}

	@Override
	public List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId) {
		return bankAccountRepository.findByUserIdAndBankAccessId(userId, bankAccessId);
	}

	@Override
	public Optional<BankAccountEntity> findByUserIdAndId(String userId, String id) {
		return bankAccountRepository.findByUserIdAndId(userId, id);
	}

	@Override
	public boolean exists(String accountId) {
		return bankAccountRepository.existsById(accountId);
	}

	@Override
	public void save(List<BankAccountEntity> bankAccounts) {
		bankAccountRepository.saveAll(bankAccounts);
	}

	@Override
	public void save(BankAccountEntity bankAccount) {
		bankAccountRepository.save(bankAccount);
	}

	@Override
	public BankAccount.SyncStatus getSyncStatus(String accountId) {
		Query where = Query.query(Criteria.where("id").is(accountId));

		where.fields().include("syncStatus");
        BankAccountEntity bankAccountEntity = mongoTemplate.findOne(where, BankAccountEntity.class);
        if (bankAccountEntity != null) {
            return bankAccountEntity.getSyncStatus();
        }

        return null;
	}

	@Override
	public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
		Query where = Query.query(Criteria.where("id").is(accountId));
		Update update = new Update().set("syncStatus", syncStatus);
		mongoTemplate.updateFirst(where, update, BankAccountEntity.class);
	}

	@Override
	public List<BankAccountEntity> deleteByBankAccess(String accessId) {
		return bankAccountRepository.deleteByBankAccessId(accessId);
	}

	@Override
	public Optional<BankAccountEntity> findOne(String accountId) {
		return bankAccountRepository.findById(accountId);
	}

}
