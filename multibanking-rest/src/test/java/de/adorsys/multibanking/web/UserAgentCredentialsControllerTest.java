package de.adorsys.multibanking.web;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.config.web.WebMvcUnitTest;
import de.adorsys.multibanking.domain.UserAgentCredentials;
import de.adorsys.multibanking.domain.UserAgentKeyEntry;
import de.adorsys.multibanking.service.UserAgentCredentialsService;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.multibanking.web.base.BaseControllerUnitTest;
import de.adorsys.multibanking.web.user.UserAgentCredentialsController;

@WebMvcUnitTest(controllers = UserAgentCredentialsController.class)
public class UserAgentCredentialsControllerTest extends BaseControllerUnitTest {

    @InjectMocks
    private UserAgentCredentialsController userAgentCredentialsController;

    @MockBean
    private UserAgentCredentialsService userAgentCredentialsService;
    
    private UserAgentCredentials userAgentCredentials;
    private String userAgentCredentialsStr;
    

	@Before
	public void setUp() throws Exception {
//		UserAgentCredentials sampleUserAgentCredentials = sampleUserAgentCredentials();
//		String writeValueAsString = mapper.writeValueAsString(sampleUserAgentCredentials);
		MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userAgentCredentialsController).build();
        InputStream stream = UserAgentCredentialsControllerTest.class.getResourceAsStream("/UserAgentCredentialsControllerTest/useragentcredentials.json");
        userAgentCredentialsStr = IOUtils.toString(stream, Charset.forName("UTF-8"));
        userAgentCredentials = mapper.readValue(userAgentCredentialsStr, UserAgentCredentials.class);
	}

	@Test
	public void testGetUserAgentCredentials200() throws Exception {		
        String userAgentId = Ids.uuid();
		BDDMockito.when(userAgentCredentialsService.load(userAgentId)).thenReturn(userAgentCredentials);
        mockMvc.perform(MockMvcRequestBuilders.get(basePath().build().toString(), userAgentId)
        		.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
        		.andExpect(MockMvcResultMatchers.content().json(userAgentCredentialsStr));
	}

	/* *******  URLS *******/
    private static final UriComponentsBuilder basePath(){
    	return UriComponentsBuilder.fromPath(UserAgentCredentialsController.BASE_PATH);
    }

	private static UserAgentCredentials sampleUserAgentCredentials() throws Exception {
		UserAgentCredentials userAgentCredentials = new UserAgentCredentials();
		userAgentCredentials.setUserAgentId(Ids.uuid());
		String keyId = Ids.uuid();
		userAgentCredentials.getKeyEntries().put(keyId, new UserAgentKeyEntry());
		UserAgentKeyEntry userAgentKeyEntry = userAgentCredentials.getKeyEntries().get(keyId);
		userAgentKeyEntry.setKeyId(keyId);
		userAgentKeyEntry.setExp(LocalDateTime.now());
		userAgentKeyEntry.getKeyData().put("secretKey", RandomStringUtils.randomAlphanumeric(16));
//		OctetSequenceKey aesKey = KeyGen.newAESKey(keyId, EncryptionMethod.A256GCM);
//		userAgentKeyEntry.setJwk(aesKey);
		return userAgentCredentials;
	}
}
