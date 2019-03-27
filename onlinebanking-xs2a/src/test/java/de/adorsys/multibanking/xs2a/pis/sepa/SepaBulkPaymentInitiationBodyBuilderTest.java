package de.adorsys.multibanking.xs2a.pis.sepa;

import de.adorsys.multibanking.domain.BulkPayment;
import de.adorsys.multibanking.domain.SinglePayment;
import de.adorsys.psd2.client.model.BulkPaymentInitiationSctJson;
import de.adorsys.psd2.client.model.PaymentInitiationSctBulkElementJson;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SepaBulkPaymentInitiationBodyBuilderTest extends AbstractSepaPaymentInitiationBodyBuilder {

    @Test
    public void buildBody() {

        SepaBulkPaymentInitiationBodyBuilder builder = new SepaBulkPaymentInitiationBodyBuilder();

        BulkPaymentInitiationSctJson body = builder.buildBody(buildBulkPayment());

        assertThat(body.getPayments()).hasSize(1);
        assertThat(body.getDebtorAccount().getIban()).isEqualTo(IBAN);

        PaymentInitiationSctBulkElementJson payment = body.getPayments().get(0);

        assertThat(payment.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(payment.getRemittanceInformationUnstructured()).isEqualTo(INFORMATION);
        assertThat(payment.getCreditorAccount().getIban()).isEqualTo(IBAN);
        assertThat(payment.getInstructedAmount().getAmount()).isEqualTo(String.valueOf(AMOUNT_VALUE));
        assertThat(payment.getInstructedAmount().getCurrency()).isEqualTo(CURRENCY);
    }

    private BulkPayment buildBulkPayment() {
        BulkPayment bulk = new BulkPayment();
        SinglePayment payment = buildSinglePayment();
        bulk.setPayments(Collections.singletonList(payment));
        bulk.setDebtorBankAccount(buildBankAccount());
        return bulk;
    }
}