package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import org.junit.Test;
import org.kapott.hbci.passport.PinTanPassport;

import java.util.HashMap;

import static org.mockito.Mockito.*;

public class ScaAwareJobTest {

    @Test(expected = MultibankingException.class)
    public void testGetKontoFailure() {
        ScaAwareJob<LoadAccounts, AccountInformationResponse> job = mock(ScaAwareJob.class);
        when(job.getTransactionRequest()).thenReturn(new TransactionRequest(new LoadAccounts()));
        when(job.getHbciKonto(any())).thenCallRealMethod();

        job.getHbciKonto(new PinTanPassport("300", new HashMap<>(), null, null));
    }
}
