package de.adorsys.multibanking.xs2a.executor;

import de.adorsys.multibanking.xs2a.model.PaymentXS2AUpdateRequest;
import de.adorsys.multibanking.xs2a.model.Xs2aTanSubmit;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.PaymentInitiationServicePisApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentUpdateRequestExecutorTest {

    private static final String SINGLE_PAYMENT_SERVICE = "payments";
    private static final String SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "authorisationID";
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final Object BODY = new Object();
    private static final String PSU_ID = "psuID";
    private static final String PSU_CORPORATE_ID = "psuCorporateID";
    private static final String PSU_IP_ADDRESS = "psuIpAddress";
    private static final String PAYMENT_ID = "paymentID";

    private PaymentUpdateRequestExecutor executor;

    @Mock
    private PaymentInitiationServicePisApi pisApi;

    @Before
    public void setUp() throws Exception {
        executor = new PaymentUpdateRequestExecutor() {
            @Override
            PaymentInitiationServicePisApi createApiClient(ApiClient apiClient) {
                return pisApi;
            }
        };
    }

    @Test
    public void execute() throws ApiException {
        when(pisApi.updatePaymentPsuData(SINGLE_PAYMENT_SERVICE, SEPA_CREDIT_TRANSFERS, PAYMENT_ID, AUTHORISATION_ID,
                REQUEST_ID, BODY, null, null, null,
                PSU_ID, null, PSU_CORPORATE_ID, null,
                PSU_IP_ADDRESS, null, null, null,
                null, null, null,
                null, null, null))
                .thenReturn(new Object());

        String consentId = executor.execute(buildRequest(), new ApiClient());

        verify(pisApi, times(1)).updatePaymentPsuData(SINGLE_PAYMENT_SERVICE,
                SEPA_CREDIT_TRANSFERS, PAYMENT_ID, AUTHORISATION_ID,
                REQUEST_ID, BODY, null, null, null,
                PSU_ID, null, PSU_CORPORATE_ID, null,
                PSU_IP_ADDRESS, null, null, null,
                null, null, null,
                null, null, null);

        assertThat(consentId).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void createRequest() {
        Xs2aTanSubmit tanSubmit = new Xs2aTanSubmit();
        tanSubmit.setTransactionId(PAYMENT_ID);
        PaymentXS2AUpdateRequest request = executor.createRequest(tanSubmit);

        assertThat(request.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(request.getService()).isEqualTo(SINGLE_PAYMENT_SERVICE);
        assertThat(request.getProduct()).isEqualTo(SEPA_CREDIT_TRANSFERS);
    }

    private PaymentXS2AUpdateRequest buildRequest() {
        PaymentXS2AUpdateRequest request = new PaymentXS2AUpdateRequest();
        request.setPaymentId(PAYMENT_ID);
        request.setService(SINGLE_PAYMENT_SERVICE);
        request.setProduct(SEPA_CREDIT_TRANSFERS);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setRequestId(REQUEST_ID);
        request.setBody(BODY);
        request.setPsuId(PSU_ID);
        request.setPsuCorporateId(PSU_CORPORATE_ID);
        request.setPsuIpAddress(PSU_IP_ADDRESS);
        return request;
    }
}