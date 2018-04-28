package de.adorsys.multibanking.config.authserver;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.utils.Ids;
import de.adorsys.multibanking.web.base.BaseControllerIT;
import de.adorsys.multibanking.web.base.PasswordGrantResponse;

@RunWith(SpringRunner.class)
public class PasswordGrantControllerTest extends BaseControllerIT {

    @Test
	public void testAuth() throws Exception {
    	PasswordGrantResponse resp = auth(Ids.uuid(), Ids.uuid());
    	Assert.assertFalse(StringUtils.isEmpty(resp.getAccessToken()));
    	// TODO: check that bearer token contains user secret.
    	Assert.assertEquals("Bearer", resp.getTokenType());
    }
}
