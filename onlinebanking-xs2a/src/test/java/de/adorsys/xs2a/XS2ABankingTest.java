package de.adorsys.xs2a;

import domain.BankAccess;
import domain.LoadAccountInformationRequest;
import domain.LoadAccountInformationResponse;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

@Ignore
public class XS2ABankingTest {

    private XS2ABanking xs2aBanking = new XS2ABanking();

    @Test
    public void testLoadBankAccounts() {
        BankAccess bankAccess = new BankAccess();
        bankAccess.setBankLogin(System.getProperty("login"));
        bankAccess.setBankLogin2(System.getProperty("login2"));

        LoadAccountInformationRequest request = LoadAccountInformationRequest.builder()
                .bankAccess(bankAccess)
                .bankCode(System.getProperty("blz"))
                .pin(System.getProperty("pin"))
                .build();

        LoadAccountInformationResponse response = xs2aBanking.loadBankAccounts(Optional.of("http://localhost:8082"), request);

    }
}
