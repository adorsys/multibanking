package de.adorsys.multibanking.web;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.web.user.UserDataController;
import domain.BankAccount.SyncStatus;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_003a_UserData extends MB_BaseTest {
    @Before
    public void setup() throws Exception {
        super.setupBank();
    }

}
