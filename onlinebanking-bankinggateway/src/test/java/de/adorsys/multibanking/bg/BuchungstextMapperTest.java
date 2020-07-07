package de.adorsys.multibanking.bg;

import org.junit.Test;

import static org.junit.Assert.*;

public class BuchungstextMapperTest {

    @Test
    public void testValidGVCodes() {
        assertEquals("Inhaberscheck (nicht eurocheque)", BuchungstextMapper.gvcode2Buchungstext("001"));
        assertEquals("Rückrechnung von Schecks", BuchungstextMapper.gvcode2Buchungstext("111"));
        assertEquals("SEPA Credit Transfer Instant (Einzelbuchung-Haben, Lohn-, Gehalts-, Rentengutschrift)", BuchungstextMapper.gvcode2Buchungstext("157"));
    }

    @Test
    public void testDefaultText() {
        assertEquals("Zahlungsverkehr in Euro innerhalb der EU und des EWR (nicht spezifiziert)", BuchungstextMapper.gvcode2Buchungstext("000"));
        assertEquals("Devisengeschäft (nicht spezifiziert)", BuchungstextMapper.gvcode2Buchungstext("499"));
        assertEquals("Sonstige (nicht spezifiziert)", BuchungstextMapper.gvcode2Buchungstext("800"));
    }

    @Test
    public void testInvalid() {
        assertNull(BuchungstextMapper.gvcode2Buchungstext("8000"));
        assertNull(BuchungstextMapper.gvcode2Buchungstext("500"));
        assertNull(BuchungstextMapper.gvcode2Buchungstext("700"));
        assertNull(BuchungstextMapper.gvcode2Buchungstext("Bla"));
    }
}
