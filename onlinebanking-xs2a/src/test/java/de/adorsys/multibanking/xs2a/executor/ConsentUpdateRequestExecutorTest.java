package de.adorsys.multibanking.xs2a.executor;

import de.adorsys.multibanking.xs2a.model.ConsentXS2AUpdateRequest;
import de.adorsys.multibanking.xs2a.model.Xs2aTanSubmit;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.AccountInformationServiceAisApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentUpdateRequestExecutorTest {

    private static final String CONSENT_ID = "consentID";
    private static final String AUTHORISATION_ID = "authorisationID";
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final Object BODY = new Object();
    private static final String PSU_ID = "psuID";
    private static final String PSU_CORPORATE_ID = "psuCorporateID";
    private static final String PSU_IP_ADDRESS = "psuIpAddress";

    private ConsentUpdateRequestExecutor executor;

    @Mock
    private AccountInformationServiceAisApi aisApi;

    @Before
    public void setUp() throws Exception {
        executor = new ConsentUpdateRequestExecutor() {
            @Override
            AccountInformationServiceAisApi createAisClient(ApiClient apiClient) {
                return aisApi;
            }
        };
    }

    @Test
    public void execute() throws ApiException {
        when(aisApi.updateConsentsPsuData(CONSENT_ID, AUTHORISATION_ID, REQUEST_ID, BODY, null,
                null, null, PSU_ID, null,
                PSU_CORPORATE_ID, null, PSU_IP_ADDRESS,
                null, null, null,
                null, null, null,
                null, null, null))
                .thenReturn(new Object());

        String consentId = executor.execute(buildRequest(), new ApiClient());

        verify(aisApi, times(1)).updateConsentsPsuData(CONSENT_ID, AUTHORISATION_ID, REQUEST_ID,
                BODY, null, null, null,
                PSU_ID, null, PSU_CORPORATE_ID, null,
                PSU_IP_ADDRESS, null, null,
                null, null,
                null, null, null,
                null, null);

        assertThat(consentId).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createRequest() {
        Xs2aTanSubmit tanSubmit = new Xs2aTanSubmit();
        tanSubmit.setTransactionId(CONSENT_ID);
        ConsentXS2AUpdateRequest request = executor.createRequest(tanSubmit);

        assertThat(request.getConsentId()).isEqualTo(CONSENT_ID);
    }

    private ConsentXS2AUpdateRequest buildRequest() {
        ConsentXS2AUpdateRequest request = new ConsentXS2AUpdateRequest();
        request.setConsentId(CONSENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setRequestId(REQUEST_ID);
        request.setBody(BODY);
        request.setPsuId(PSU_ID);
        request.setPsuCorporateId(PSU_CORPORATE_ID);
        request.setPsuIpAddress(PSU_IP_ADDRESS);
        return request;
    }
}