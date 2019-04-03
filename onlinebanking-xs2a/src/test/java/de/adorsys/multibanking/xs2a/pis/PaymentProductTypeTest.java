package de.adorsys.multibanking.xs2a.pis;

import de.adorsys.multibanking.xs2a.error.XS2AClientException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentProductTypeTest {

    @Test
    public void getType() {
        assertThat(PaymentProductType.SEPA.getType()).isEqualTo("sepa-credit-transfers");
        assertThat(PaymentProductType.INSTANT_SEPA.getType()).isEqualTo("instant-sepa-credit-transfers");
        assertThat(PaymentProductType.TARGET_2_PAYMENTS.getType()).isEqualTo("target-2-payments");
        assertThat(PaymentProductType.CROSS_BORDER.getType()).isEqualTo("cross-border-credit-transfers");
        assertThat(PaymentProductType.PAIN_001_SEPA.getType()).isEqualTo("pain.001-sepa-credit-transfers");
        assertThat(PaymentProductType.PAIN_001_INSTANT_SEPA.getType()).isEqualTo("pain.001-instant-sepa-credit-transfers");
        assertThat(PaymentProductType.PAIN_001_TARGET_2_PAYMENTS.getType()).isEqualTo("pain.001-target-2-payments");
        assertThat(PaymentProductType.PAIN_001_CROSS_BORDER.getType()).isEqualTo("pain.001-cross-border-credit-transfers");
    }


    @Test
    public void isRaw() {
        assertThat(PaymentProductType.SEPA.isRaw()).isFalse();
        assertThat(PaymentProductType.INSTANT_SEPA.isRaw()).isFalse();
        assertThat(PaymentProductType.TARGET_2_PAYMENTS.isRaw()).isFalse();
        assertThat(PaymentProductType.CROSS_BORDER.isRaw()).isFalse();
        assertThat(PaymentProductType.PAIN_001_SEPA.isRaw()).isTrue();
        assertThat(PaymentProductType.PAIN_001_INSTANT_SEPA.isRaw()).isTrue();
        assertThat(PaymentProductType.PAIN_001_TARGET_2_PAYMENTS.isRaw()).isTrue();
        assertThat(PaymentProductType.PAIN_001_CROSS_BORDER.isRaw()).isTrue();
    }

    @Test
    public void resolve() {
        PaymentProductType type = PaymentProductType.resolve("sepa-credit-transfers");

        assertThat(type).isEqualTo(PaymentProductType.SEPA);
    }

    @Test(expected = XS2AClientException.class)
    public void resolveUnknown() {
        PaymentProductType.resolve("new-product");
    }
}