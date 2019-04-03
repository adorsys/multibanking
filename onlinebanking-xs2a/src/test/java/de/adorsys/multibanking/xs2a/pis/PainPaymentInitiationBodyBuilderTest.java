package de.adorsys.multibanking.xs2a.pis;

import de.adorsys.multibanking.domain.RawSepaPayment;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PainPaymentInitiationBodyBuilderTest {

    @Test
    public void buildBody() {
        RawSepaPayment payment = new RawSepaPayment();
        payment.setPainXml("payment in xml format");
        byte[] bytes = new PainPaymentInitiationBodyBuilder().buildBody(payment);

        assertThat(bytes).isNotNull();
    }
}