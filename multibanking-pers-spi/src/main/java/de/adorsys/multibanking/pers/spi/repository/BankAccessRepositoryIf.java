package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BankAccessEntity;

import java.util.List;
import java.util.Optional;

public interface BankAccessRepositoryIf {

    Optional<BankAccessEntity> findByUserIdAndId(String userId, String id);

    List<BankAccessEntity> findByUserId(String userId);

    void save(BankAccessEntity bankAccess);

    String getBankCode(String id);

    boolean exists(String accessId);

    boolean deleteByUserIdAndBankAccessId(String userId, String bankAccessId);
}
