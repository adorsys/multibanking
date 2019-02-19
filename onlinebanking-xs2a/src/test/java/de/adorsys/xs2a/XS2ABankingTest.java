package de.adorsys.xs2a;

import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.PaymentInitiationServicePisApi;
import de.adorsys.psd2.client.model.*;
import de.adorsys.xs2a.error.XS2AClientException;
import domain.BankAccess;
import domain.TanTransportType;
import domain.request.AuthenticatePsuRequest;
import domain.request.LoadAccountInformationRequest;
import domain.response.LoadAccountInformationResponse;
import domain.response.ScaMethodsResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.xs2a.XS2ABanking.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XS2ABankingTest {

    private XS2ABanking xs2aBanking;

    @Mock
    private ApiClient apiClient;

    @Mock
    private PaymentInitiationServicePisApi paymentInitiationServicePisApi;

    @Before
    public void setUp() {
        xs2aBanking = new XS2ABanking() {

            @Override
            ApiClient createApiClient(String bankingUrl) {
                return apiClient;
            }

            @Override
            PaymentInitiationServicePisApi createPaymentInitiationServicePisApi(ApiClient apiClient) {
                return paymentInitiationServicePisApi;
            }
        };
    }

    @Test
    public void authenticatePsu() throws ApiException {
        String paymentId = "paymentId";
        String psuId = "login";
        String custId = "custId";
        String authorisationId = "xs2a-authorisationId";
        String pin = "pin";
        AuthenticatePsuRequest request = AuthenticatePsuRequest.builder()
                                                 .bankCode("08098")
                                                 .customerId(custId)
                                                 .login(psuId)
                                                 .paymentId(paymentId)
                                                 .pin(pin)
                                                 .build();

        StartScaprocessResponse scaProcessResponse = new StartScaprocessResponse();
        ScaMethods scaMethods = new ScaMethods();
        AuthenticationObject method = new AuthenticationObject();
        method.setAuthenticationMethodId("111");
        method.setAuthenticationType(AuthenticationType.PHOTO_OTP);
        method.setAuthenticationVersion("v1.0");
        method.setExplanation("some explanation");
        method.setName("Photo Tan");
        scaMethods.add(method);
        scaProcessResponse.setScaMethods(scaMethods);
        Map<String, String> links = new HashMap<>();
        links.put("startAuthorisationWithPsuAuthentication", "https://bkv-xs2a-dev.cloud.adorsys.de/v1/payments/sepa-credit-transfers/" + authorisationId);
        scaProcessResponse.setLinks(links);
        ArgumentCaptor<UpdatePsuAuthentication> psuBodyCaptor = ArgumentCaptor.forClass(UpdatePsuAuthentication.class);

        when(paymentInitiationServicePisApi.startPaymentAuthorisation(eq(SINGLE_PAYMENT_SERVICE), eq(SEPA_CREDIT_TRANSFERS),
                                                                      eq(paymentId), any(), eq(psuId), isNull(), isNull(),
                                                                      isNull(), isNull(), isNull(),
                                                                      isNull(), eq(PS_UIP_ADDRESS),
                                                                      isNull(), isNull(),
                                                                      isNull(), isNull(),
                                                                      isNull(), isNull(),
                                                                      isNull(), isNull(), isNull())
        ).thenReturn(scaProcessResponse);

        when(paymentInitiationServicePisApi.updatePaymentPsuData(eq(SINGLE_PAYMENT_SERVICE), eq(SEPA_CREDIT_TRANSFERS), eq(paymentId),
                                                                 eq(authorisationId), any(), psuBodyCaptor.capture(),
                                                                 isNull(), isNull(), isNull(),
                                                                 eq(psuId), isNull(), eq(custId),
                                                                 isNull(), eq(PS_UIP_ADDRESS), isNull(),
                                                                 isNull(), isNull(), isNull(),
                                                                 isNull(), isNull(),
                                                                 isNull(), isNull(), isNull())
        ).thenReturn(scaProcessResponse);

        ScaMethodsResponse response = xs2aBanking.authenticatePsu("bankUrl", request);

        assertThat(response.getTanTransportTypes()).hasSize(1);
        assertThat(response.getAuthorizationId()).isEqualTo(authorisationId);

        TanTransportType tanTransportType = response.getTanTransportTypes().get(0);

        assertThat(method.getAuthenticationMethodId()).isEqualTo(tanTransportType.getId());
        assertThat(method.getAuthenticationVersion()).isEqualTo(tanTransportType.getMedium());
        assertThat(method.getName()).isEqualTo(tanTransportType.getName());
        assertThat(method.getExplanation()).isEqualTo(tanTransportType.getInputInfo());

        UpdatePsuAuthentication psuAuthentication = psuBodyCaptor.getValue();

        assertThat(psuAuthentication.getPsuData().getPassword()).isEqualTo(pin);
    }

    @Test(expected = XS2AClientException.class)
    public void authorisePsuWithException() throws ApiException {
        String paymentId = "paymentId";
        String psuId = "login";
        String custId = "custId";
        String pin = "pin";

        AuthenticatePsuRequest request = AuthenticatePsuRequest.builder()
                                                 .bankCode("08098")
                                                 .customerId(custId)
                                                 .login(psuId)
                                                 .paymentId(paymentId)
                                                 .pin(pin)
                                                 .build();

        when(paymentInitiationServicePisApi.startPaymentAuthorisation(eq(SINGLE_PAYMENT_SERVICE), eq(SEPA_CREDIT_TRANSFERS),
                                                                      eq(paymentId), any(), eq(psuId), isNull(), isNull(),
                                                                      isNull(), isNull(), isNull(),
                                                                      isNull(), eq(PS_UIP_ADDRESS),
                                                                      isNull(), isNull(),
                                                                      isNull(), isNull(),
                                                                      isNull(), isNull(),
                                                                      isNull(), isNull(), isNull())).thenThrow(ApiException.class);

        xs2aBanking.authenticatePsu("bankUrl", request);
    }

    @Ignore
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

        LoadAccountInformationResponse response = xs2aBanking.loadBankAccounts("http://localhost:8082", request);

    }
}
