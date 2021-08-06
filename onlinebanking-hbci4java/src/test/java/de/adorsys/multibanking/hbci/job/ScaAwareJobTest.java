package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.hbci.HbciBpdCacheHolder;
import org.junit.Test;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.HashMap;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class ScaAwareJobTest {

    @Test
    public void testGetKontoFailure() throws NoSuchFieldException {
        ScaAwareJob<LoadAccounts, AccountInformationResponse> job = mock(AccountInformationJob.class,
            withSettings()
                .useConstructor(new TransactionRequest<>(new LoadAccounts()), new HbciBpdCacheHolder(0))
                .defaultAnswer(CALLS_REAL_METHODS));

        AbstractHbciDialog dialog = mock(HBCIJobsDialog.class);
        when(dialog.getPassport()).thenReturn(new PinTanPassport("300", new HashMap<>(), null, null));

        FieldSetter.setField(job, ScaAwareJob.class.getDeclaredField("dialog"), dialog);

        assertNull("HbciKonto must be null", job.getHbciKonto());
    }
}
