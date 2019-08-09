package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.jpa.entity.BankAccessJpaEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.BankAccessRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class BankAccessRepositoryImpl implements BankAccessRepositoryIf {

    private final BankAccessRepositoryJpa bankAccessRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public Optional<BankAccessEntity> findByUserIdAndId(String userId, String id) {
        return bankAccessRepository.findByUserIdAndId(userId, new Long(id))
                .map(entityMapper::mapToBankAccessEntity);
    }

    @Override
    public Optional<BankAccessEntity> findOne(String id) {
        return bankAccessRepository.findById(new Long(id))
                .map(entityMapper::mapToBankAccessEntity);
    }

    @Override
    public List<BankAccessEntity> findByUserId(String userId) {
        return entityMapper.mapToBankAccessEntities(bankAccessRepository.findByUserId(userId));
    }

    @Override
    public List<BankAccessEntity> findByUserIdAndConsentId(String userId) {
        return entityMapper.mapToBankAccessEntities(bankAccessRepository.findByUserIdAndConsentId(userId));
    }

    @Override
    public void save(BankAccessEntity bankAccess) {
        BankAccessJpaEntity bankAccessMongoEntity =
                bankAccessRepository.save(entityMapper.mapToBankAccessJpaEntity(bankAccess));
        bankAccess.setId(bankAccessMongoEntity.getId().toString());
    }

    @Override
    public String getBankCode(String id) {
        return bankAccessRepository.getBankCode(new Long(id));
    }

    @Override
    public boolean exists(String id) {
        return bankAccessRepository.existsById(new Long(id));
    }

    @Override
    public boolean deleteByUserIdAndBankAccessId(String userId, String id) {
        bankAccessRepository.deleteByUserIdAndId(userId, new Long(id));
        return true;
    }
}
