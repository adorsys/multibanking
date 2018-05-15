package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.domain.BankAccessData;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peter on 04.05.18 at 16:17.
 * MUL-269
 */
public class UserDataTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserDataTest.class);
    @Test
    public void testDelete() {
        String ID = "ID1";
        UserData userData = new UserData();
        BankAccessData bankAccessData = new BankAccessData();
        BankAccessEntity bankAccessEntity = new BankAccessEntity();
        bankAccessEntity.setBankCode("123");
        bankAccessEntity.setBankLogin("login");
        bankAccessEntity.setPin("1234");
        bankAccessEntity.setId(ID);
        bankAccessData.setBankAccess(bankAccessEntity);
        userData.put(ID, bankAccessData);
        LOGGER.debug("userData:" + userData);

        BankAccessData bankAccessData1 = userData.remove(ID);
        LOGGER.debug("bankAccessData     orig:" + bankAccessData);
        LOGGER.debug("bankAccessData  deleted:" + bankAccessData1);
        Assert.assertNotNull(bankAccessData1);
        Assert.assertEquals(bankAccessData, bankAccessData1);
        BankAccessData bankAccessData2 = userData.remove(ID);
        LOGGER.debug("bankAccessData deleted2:" + bankAccessData2);
        Assert.assertNull(bankAccessData2);
    }
}
