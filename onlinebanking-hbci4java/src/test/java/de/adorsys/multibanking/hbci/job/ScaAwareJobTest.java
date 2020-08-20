package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import org.junit.Test;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.HashMap;

import static org.mockito.Mockito.*;

public class ScaAwareJobTest {

    @Test(expected = MultibankingException.class)
    public void testGetKontoFailure() throws NoSuchFieldException {
        ScaAwareJob<LoadAccounts, AccountInformationResponse> job = mock(AccountInformationJob.class,
            withSettings()
                .useConstructor(new TransactionRequest<>(new LoadAccounts()))
                .defaultAnswer(CALLS_REAL_METHODS));

        AbstractHbciDialog dialog = mock(HBCIJobsDialog.class);
        when(dialog.getPassport()).thenReturn(new PinTanPassport("300", new HashMap<>(), null, null));

        FieldSetter.setField(job, ScaAwareJob.class.getDeclaredField("dialog"), dialog);

        job.getHbciKonto();
    }
}
