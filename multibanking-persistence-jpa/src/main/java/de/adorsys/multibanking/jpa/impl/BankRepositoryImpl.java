package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.jpa.entity.BankJpaEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.BankRepositoryJpa;
import de.adorsys.multibanking.jpa.repository.HibernateSearchService;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Profile({"jpa"})
@Repository
public class BankRepositoryImpl implements BankRepositoryIf {

    private final BankRepositoryJpa bankRepository;
    private final JpaEntityMapper entityMapper;
    private final HibernateSearchService hibernateSearchService;

    @Override
    public Optional<String> findBankingUrl(String bankCode) {
        return Optional.ofNullable(bankRepository.findBankingUrl(bankCode));
    }

    @Override
    public Optional<BankEntity> findByBankCode(String blz) {
        return bankRepository.findByBankCode(blz)
                .map(entityMapper::mapToBankEntity);
    }

    @Override
    public void save(Iterable<BankEntity> bankEntities) {
        bankRepository.saveAll(entityMapper.mapToBankJpaEntities(bankEntities));
    }

    @Override
    public void deleteAll() {
        bankRepository.deleteAll();
    }

    @Override
    public void save(BankEntity bank) {
        bankRepository.save(entityMapper.mapToBankJpaEntity(bank));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BankEntity> search(String text) {
        FullTextQuery fullTextQuery = hibernateSearchService.searchBank(text);

        fullTextQuery.setMaxResults(10);

        List<BankJpaEntity> resultList = fullTextQuery.getResultList();

        return entityMapper.mapToBankEntities(resultList);

    }

}
