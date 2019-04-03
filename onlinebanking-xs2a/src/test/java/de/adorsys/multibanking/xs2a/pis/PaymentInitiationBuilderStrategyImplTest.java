package de.adorsys.multibanking.xs2a.pis;

import de.adorsys.multibanking.xs2a.error.XS2AClientException;
import de.adorsys.multibanking.xs2a.pis.sepa.SepaBulkPaymentInitiationBodyBuilder;
import de.adorsys.multibanking.xs2a.pis.sepa.SepaPeriodicPaymentInitiationBodyBuilder;
import de.adorsys.multibanking.xs2a.pis.sepa.SepaSinglePaymentInitiationBodyBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentInitiationBuilderStrategyImplTest {

    private PaymentInitiationBuilderStrategy builderStrategy = new PaymentInitiationBuilderStrategyImpl();

    @Test
    public void resolve() {
        PaymentInitiationBodyBuilder builder = builderStrategy.resolve(PaymentProductType.SEPA, PaymentServiceType.SINGLE);
        assertThat(builder.getClass()).isAssignableFrom(SepaSinglePaymentInitiationBodyBuilder.class);

        builder = builderStrategy.resolve(PaymentProductType.SEPA, PaymentServiceType.PERIODIC);
        assertThat(builder.getClass()).isAssignableFrom(SepaPeriodicPaymentInitiationBodyBuilder.class);

        builder = builderStrategy.resolve(PaymentProductType.SEPA, PaymentServiceType.BULK);
        assertThat(builder.getClass()).isAssignableFrom(SepaBulkPaymentInitiationBodyBuilder.class);

        builder = builderStrategy.resolve(PaymentProductType.PAIN_001_SEPA, PaymentServiceType.SINGLE);
        assertThat(builder.getClass()).isAssignableFrom(PainPaymentInitiationBodyBuilder.class);
    }

    @Test(expected = XS2AClientException.class)
    public void resolveNotSupportedProduct() {
        builderStrategy.resolve(PaymentProductType.INSTANT_SEPA, PaymentServiceType.SINGLE);
    }
}