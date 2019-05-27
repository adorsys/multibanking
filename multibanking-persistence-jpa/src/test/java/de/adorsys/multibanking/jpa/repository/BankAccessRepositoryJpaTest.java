package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.jpa.conf.JpaConfig;
import de.adorsys.multibanking.jpa.conf.MapperConfig;
import de.adorsys.multibanking.jpa.entity.BankAccessJpaEntity;
import de.adorsys.multibanking.jpa.impl.BankAccessRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JpaConfig.class, MapperConfig.class, BankAccessRepositoryImpl.class})
@RunWith(SpringRunner.class)
public class BankAccessRepositoryJpaTest {

    @Autowired
    private BankAccessRepositoryImpl repository;

    @Test
    public void test() {
        BankAccessEntity bankAccess = createBankAccess();

        assertThat(repository.exists(bankAccess.getId())).isTrue();

        assertThat(repository.findByUserId(bankAccess.getUserId())).isNotEmpty();

        assertThat(repository.findByUserIdAndId(bankAccess.getUserId(), bankAccess.getId())).isNotEmpty();

        assertThat(repository.getBankCode(bankAccess.getId())).isNotBlank();

        assertThat(repository.getBankCode("2")).isNull();

        assertThat(repository.deleteByUserIdAndBankAccessId(bankAccess.getUserId(), bankAccess.getId())).isTrue();

        assertThat(repository.exists(bankAccess.getId())).isFalse();
    }

    public BankAccessEntity createBankAccess() {
        BankAccessEntity entity = new BankAccessEntity();
        entity.setUserId(UUID.randomUUID().toString());
        entity.setBankCode(UUID.randomUUID().toString());

        repository.save(entity);

        return entity;
    }

}
