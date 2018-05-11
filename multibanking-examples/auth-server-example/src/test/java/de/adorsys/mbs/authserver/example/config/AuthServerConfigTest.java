package de.adorsys.mbs.authserver.example.config;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
//@WebMvcTest
public class AuthServerConfigTest extends BaseControllerIT {
    public final static Logger LOGGER = LoggerFactory.getLogger(AuthServerConfigTest.class);

	@Test
	public void test() {
    	PasswordGrantResponse resp = auth(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    	Assert.assertFalse(StringUtils.isEmpty(resp.getAccessToken()));
    	// TODO: check that bearer token contains user secret.
    	Assert.assertEquals("Bearer", resp.getTokenType());
	}

}
