package de.adorsys.xs2a.pis;

import de.adorsys.xs2a.error.XS2AClientException;
import domain.BulkPayment;
import domain.FutureBulkPayment;
import domain.FutureSinglePayment;
import domain.SinglePayment;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentServiceTypeTest {

    @Test
    public void getType() {
        assertThat(PaymentServiceType.SINGLE.getType()).isEqualTo("payments");
        assertThat(PaymentServiceType.BULK.getType()).isEqualTo("bulk-payments");
        assertThat(PaymentServiceType.PERIODIC.getType()).isEqualTo("periodic-payments");
    }

    @Test
    public void getClazz() {
        assertThat(PaymentServiceType.SINGLE.getClazz()).isEqualTo(SinglePayment.class);
        assertThat(PaymentServiceType.BULK.getClazz()).isEqualTo(BulkPayment.class);
        assertThat(PaymentServiceType.PERIODIC.getClazz()).isEqualTo(FutureSinglePayment.class);
    }

    @Test
    public void resolveByService() {
        PaymentServiceType serviceType = PaymentServiceType.resolve("bulk-payments");
        assertThat(serviceType).isEqualTo(PaymentServiceType.BULK);
    }

    @Test(expected = XS2AClientException.class)
    public void resolveByServiceThatNotSupported() {
        PaymentServiceType.resolve("single-payments");
    }

    @Test
    public void resolveByClass() {
        PaymentServiceType serviceType = PaymentServiceType.resolve(FutureSinglePayment.class);
        assertThat(serviceType).isEqualTo(PaymentServiceType.PERIODIC);
    }

    @Test(expected = XS2AClientException.class)
    public void resolveByClassThatNotSupported() {
        PaymentServiceType.resolve(FutureBulkPayment.class);
    }
}