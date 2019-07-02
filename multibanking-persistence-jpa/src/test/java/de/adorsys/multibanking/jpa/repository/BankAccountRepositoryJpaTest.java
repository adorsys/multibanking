package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.jpa.conf.JpaConfig;
import de.adorsys.multibanking.jpa.conf.MapperConfig;
import de.adorsys.multibanking.jpa.impl.BankAccountRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JpaConfig.class, MapperConfig.class, BankAccountRepositoryImpl.class})
@RunWith(SpringRunner.class)
public class BankAccountRepositoryJpaTest {

    @Autowired
    private BankAccountRepositoryImpl repository;

    @Test
    public void test() {
        BankAccountEntity bankAccount = createBankAccount();

        repository.save(bankAccount);

        assertThat(repository.exists(bankAccount.getId())).isTrue();

        assertThat(repository.findByUserId(bankAccount.getUserId())).isNotEmpty();

        List<BankAccountEntity> bankAccounts =
                repository.findByUserIdAndBankAccessId(bankAccount.getUserId(), bankAccount.getBankAccessId());

        assertThat(bankAccounts).isNotEmpty();

        assertThat(bankAccounts).hasSize(1);

        assertThat(bankAccounts.get(0).getBankName()).isEqualTo(bankAccount.getBankName());
    }

    public BankAccountEntity createBankAccount() {
        BankAccountEntity entity = new BankAccountEntity();
        entity.setUserId(UUID.randomUUID().toString());
        entity.setBankAccessId(UUID.randomUUID().toString());
        entity.setIban("DE123456789");
        entity.setBankName("Test-Bank");
        entity.setAccountNumber("123456678");

        repository.save(entity);

        return entity;
    }
}
