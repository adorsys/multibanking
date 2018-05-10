package de.adorsys.multibanking.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.web.user.UserDataController;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_003a_UserData extends MB_BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MB_003a_UserData.class);


    @Before
    public void setup() throws Exception {
        super.setupBank();
    }

    @Test
    public void test_1() {
    	MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserData userData = loadUserData(this);
        Assert.assertNotNull(userData);
    }

    public static UserData loadUserData(MB_BaseTest base){
    	return base.testRestTemplate.getForEntity(base.path(UserDataController.BASE_PATH).build().toUri(), UserData.class).getBody();
    }
}
