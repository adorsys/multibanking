package de.adorsys.multibanking.bg;

import de.adorsys.multibanking.bg.mapper.BankingGatewayMapper;
import de.adorsys.multibanking.bg.mapper.BankingGatewayMapperImpl;
import de.adorsys.multibanking.bg.mapper.BuchungstextMapper;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.xs2a_adapter.model.TransactionDetails;
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

    @Test
    public void testBankTransactionCode() {
        assertEquals("SEPA Credit Transfer", BuchungstextMapper.bankTransactionCode2Buchungstext("PMNT-ICDT-ESCT"));
        assertEquals("Unstrukturierte Belegung", BuchungstextMapper.bankTransactionCode2Buchungstext("XTND-NTAV-NTAV"));
        assertNull(BuchungstextMapper.bankTransactionCode2Buchungstext("xx"));
    }

    @Test
    public void testBankingGatewayMapper() {
        BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setBankTransactionCode("PMNT-ICDT-ESCT");
        Booking booking = bankingGatewayMapper.toBooking(transactionDetails);
        assertEquals("SEPA Credit Transfer", booking.getText());

        transactionDetails.setProprietaryBankTransactionCode("BLI+157+BLUB");
        Booking booking2 = bankingGatewayMapper.toBooking(transactionDetails);
        assertEquals("SEPA Credit Transfer Instant (Einzelbuchung-Haben, Lohn-, Gehalts-, Rentengutschrift)", booking2.getText());
    }
}
