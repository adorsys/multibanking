package de.adorsys.multibanking.logging;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class WhitelistMaskEverythingLayoutTest {

    @Test
    public void testJavaobject() throws Exception {
        String message = IOUtils.toString(WhitelistMaskEverythingLayoutTest.class.getResource("/testMessage.txt").toURI(), "UTF-8");
        WhitelistMaskEverythingLayout whitelistMaskEverythingLayout = new WhitelistMaskEverythingLayout();
        whitelistMaskEverythingLayout.addWhitelistEntry("transactionCode");
        whitelistMaskEverythingLayout.addWhitelistEntry("bankApi");

        System.out.println(whitelistMaskEverythingLayout.maskMessage(message));
    }

    @Test
    public void testJson() throws Exception {
        String message = IOUtils.toString(WhitelistMaskEverythingLayoutTest.class.getResource("/transactions.json").toURI(), "UTF-8");
        WhitelistMaskEverythingLayout whitelistMaskEverythingLayout = new WhitelistMaskEverythingLayout();
        whitelistMaskEverythingLayout.addWhitelistEntry("proprietaryBankTransactionCode");
        whitelistMaskEverythingLayout.addWhitelistEntry("amount");

        System.out.println(whitelistMaskEverythingLayout.maskMessage(message));
    }
}
