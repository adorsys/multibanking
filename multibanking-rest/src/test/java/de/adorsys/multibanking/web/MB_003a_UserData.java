package de.adorsys.multibanking.web;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

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
