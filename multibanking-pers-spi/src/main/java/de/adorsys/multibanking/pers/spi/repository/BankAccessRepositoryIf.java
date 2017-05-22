package de.adorsys.multibanking.pers.spi.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.multibanking.domain.BankAccessEntity;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface BankAccessRepositoryIf {

    Optional<BankAccessEntity> findByUserIdAndId(String userId, String id);

    List<BankAccessEntity> findByUserId(String userId);

	BankAccessEntity save(BankAccessEntity bankAccess);
}
