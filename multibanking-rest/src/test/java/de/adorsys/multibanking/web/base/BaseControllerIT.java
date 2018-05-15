package de.adorsys.multibanking.web.base;

import de.adorsys.multibanking.config.service.Tp;
import de.adorsys.multibanking.service.old.TestConstants;
import de.adorsys.multibanking.web.base.entity.UserPasswordTuple;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

@RunWith(SpringRunner.class)
@ActiveProfiles({"InMemory", "IntegrationTest"})
@SpringBootTest(properties = {Tp.p1, Tp.p2, Tp.p3, Tp.p4, Tp.p5, Tp.p6, Tp.p7, Tp.p8, Tp.p9, Tp.p10, Tp.p11, Tp.p12,
Tp.p13, Tp.p14, Tp.p15, Tp.p16, Tp.p17, Tp.p18, Tp.p19, Tp.p20, Tp.p21, Tp.p22, Tp.p23,
Tp.p24, Tp.p25, Tp.p26, Tp.p27, Tp.p28, Tp.p29, Tp.p30, Tp.p31, Tp.p32, Tp.p33, Tp.p34, Tp.p35, Tp.p36, Tp.p37, Tp.p38, Tp.p39, Tp.p40},
webEnvironment = WebEnvironment.DEFINED_PORT)
public abstract class BaseControllerIT {
    public final static Logger LOGGER = LoggerFactory.getLogger(BaseControllerIT.class);

    @LocalServerPort
    public int port = 8080;

    @Autowired
    public TestRestTemplate testRestTemplate;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    /**
     * <h3>Return the current test environment base uri.</h3>
     * <p>
     * <p>
     * This is due to the fact that, spring bootstrap the testing environment on
     * a random port, to not interfere with any boot application running on a
     * default port.
     * <p>
     * So we need to manually build the baseUri for the testing environment.
     * </p>
     *
     * @return baseUri String
     */
    protected String getBaseUri() {
        LOGGER.debug("== Test Port ist " + port + "==");
        return "http://localhost:" + port;
    }

    protected PasswordGrantResponse auth(UserPasswordTuple userPasswordTuple) {
        URI uri = authPath()
        .queryParam("grant_type", "password")
        .queryParam("username", userPasswordTuple.getUser())
        .queryParam("password", userPasswordTuple.getPassword())
        .queryParam("audience", "multibanking")
        .build().toUri();

        PasswordGrantResponse resp = testRestTemplate.getForObject(uri, PasswordGrantResponse.class);

        final String accessTokenValue = resp.getAccessToken();

        testRestTemplate.getRestTemplate().setInterceptors(
        Collections.singletonList((request, body, execution) -> {
            String authHeader = "Bearer " + accessTokenValue;
            request.getHeaders().add("Authorization", authHeader);
            return execution.execute(request, body);
        }));

        return resp;
    }

    protected final UriComponentsBuilder authPath() {
        return path("/token/password-grant");
    }

    public final UriComponentsBuilder path(String path) {
        return UriComponentsBuilder.fromUriString(getBaseUri()).path(path);
    }

    private static class StatusCodeInterceptor implements ClientHttpRequestInterceptor {
        private int[] expectedStatusCodes = null;
        RuntimeException setterStack;

        public StatusCodeInterceptor(int ... expectedStatusCodes) {
            setNextExpectedStatusCodes(expectedStatusCodes);
        }

        public void setNextExpectedStatusCodes(int... statusCodes) {
            this.expectedStatusCodes = statusCodes;
            this.setterStack = new RuntimeException();
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] bytes, ClientHttpRequestExecution execution) throws IOException {
            ClientHttpResponse response = execution.execute(request, bytes);
            LOGGER.debug("statusCode for " + request.getURI().toString() + " is:" + response.getStatusCode());
            if (expectedStatusCodes == null) {
                return response;
            }

            int statusCode = response.getRawStatusCode();
            for (int i = 0; i < expectedStatusCodes.length; i++) {
                if (expectedStatusCodes[i] == statusCode) {
                    // deactivate interceptor
                    expectedStatusCodes = null;
                    return response;
                }
            }
            RuntimeException assertException = new RuntimeException("statusCode for " + request.getURI().toString() + " should have been in :" + show(expectedStatusCodes) + " but was " + statusCode);
            assertException.setStackTrace(setterStack.getStackTrace());
            expectedStatusCodes = null;
            throw assertException;
        }
    }

    private StatusCodeInterceptor i = null;

    public void setNextExpectedStatusCode(int ... statusCodes) {
        if (i == null) {
            i = new StatusCodeInterceptor(statusCodes);
            testRestTemplate.getRestTemplate().getInterceptors().add(i);
        } else {
            i.setNextExpectedStatusCodes(statusCodes);
        }
    }

    private static String show(int[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<a.length; i++) {
            sb.append(a[i]);
            sb.append(" ");
        }
        return sb.toString();
    }
}
