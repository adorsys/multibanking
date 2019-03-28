package de.adorsys.multibanking.xs2a.pis;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.xs2a.error.XS2AClientException;
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
    public void resolveByPaymentObject() {
        PaymentServiceType serviceType = PaymentServiceType.resolve(new FutureSinglePayment());
        assertThat(serviceType).isEqualTo(PaymentServiceType.PERIODIC);

        serviceType = PaymentServiceType.resolve(new SinglePayment());
        assertThat(serviceType).isEqualTo(PaymentServiceType.SINGLE);

        serviceType = PaymentServiceType.resolve(new BulkPayment());
        assertThat(serviceType).isEqualTo(PaymentServiceType.BULK);

        RawSepaPayment payment = new RawSepaPayment();
        payment.setService("payments");
        payment.setPainXml("payment in xml format");
        serviceType = PaymentServiceType.resolve(payment);
        assertThat(serviceType).isEqualTo(PaymentServiceType.SINGLE);
    }

    @Test(expected = XS2AClientException.class)
    public void resolvePaymentObjectNotSupported() {
        PaymentServiceType.resolve(new FutureBulkPayment());
    }

    @Test(expected = XS2AClientException.class)
    public void resolveRawDataIsAbsent() {
        PaymentServiceType.resolve(new RawSepaPayment());
    }
}