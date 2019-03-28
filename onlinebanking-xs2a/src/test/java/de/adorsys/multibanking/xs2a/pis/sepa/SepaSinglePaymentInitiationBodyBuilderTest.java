package de.adorsys.multibanking.xs2a.pis.sepa;

import de.adorsys.multibanking.domain.SinglePayment;
import de.adorsys.psd2.client.model.PaymentInitiationSctJson;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SepaSinglePaymentInitiationBodyBuilderTest extends AbstractSepaPaymentInitiationBodyBuilder {

    @Test
    public void buildBody() {

        SinglePayment payment = buildSinglePayment();

        SepaSinglePaymentInitiationBodyBuilder builder = new SepaSinglePaymentInitiationBodyBuilder();

        PaymentInitiationSctJson body = builder.buildBody(payment);

        assertThat(body.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(body.getRemittanceInformationUnstructured()).isEqualTo(INFORMATION);
        assertThat(body.getCreditorAccount().getIban()).isEqualTo(IBAN);
        assertThat(body.getDebtorAccount().getIban()).isEqualTo(IBAN);
        assertThat(body.getInstructedAmount().getAmount()).isEqualTo(String.valueOf(AMOUNT_VALUE));
        assertThat(body.getInstructedAmount().getCurrency()).isEqualTo(CURRENCY);
        assertThat(body.getDebtorAccount().getCurrency()).isEqualTo(CURRENCY);
        assertThat(body.getCreditorAccount().getCurrency()).isEqualTo(CURRENCY);
    }

}