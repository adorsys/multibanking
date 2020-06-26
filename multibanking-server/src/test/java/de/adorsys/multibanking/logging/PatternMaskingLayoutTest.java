package de.adorsys.multibanking.logging;

import org.junit.Assert;
import org.junit.Test;

public class PatternMaskingLayoutTest {

    @Test
    public void testMaskUsage() {
        String testString = "primanota=null, usage=Wichtig, da es so ist IBAN: DE47110815, BIC: BLIBLAXY, addkey=null, sepa=false,";
        String testPattern = "(?>usage)\\=(.*?)[)|,](?= addkey=)";

        PatternMaskingLayout patternMaskingLayout = new PatternMaskingLayout();
        patternMaskingLayout.addMaskPattern(testPattern);
        String maskedString = patternMaskingLayout.maskMessage(testString);

        Assert.assertEquals("Mask pattern does not work", "primanota=null, usage=****###****, addkey=null, sepa=false,", maskedString);
    }
}
