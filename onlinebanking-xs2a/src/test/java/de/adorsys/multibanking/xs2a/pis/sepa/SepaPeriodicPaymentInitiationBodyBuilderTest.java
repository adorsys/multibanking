package de.adorsys.multibanking.xs2a.pis.sepa;

import de.adorsys.multibanking.domain.FutureSinglePayment;
import de.adorsys.multibanking.xs2a.pis.PaymentProductType;
import de.adorsys.psd2.client.model.DayOfExecution;
import de.adorsys.psd2.client.model.PeriodicPaymentInitiationSctJson;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SepaPeriodicPaymentInitiationBodyBuilderTest extends AbstractSepaPaymentInitiationBodyBuilder {

    @Test
    public void buildBody() {

        SepaPeriodicPaymentInitiationBodyBuilder builder = new SepaPeriodicPaymentInitiationBodyBuilder();

        PeriodicPaymentInitiationSctJson body = builder.buildBody(buildPeriodicPayment());

        assertThat(body.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(body.getRemittanceInformationUnstructured()).isEqualTo(INFORMATION);
        assertThat(body.getCreditorAccount().getIban()).isEqualTo(IBAN);
        assertThat(body.getDebtorAccount().getIban()).isEqualTo(IBAN);
        assertThat(body.getInstructedAmount().getAmount()).isEqualTo(String.valueOf(AMOUNT_VALUE));
        assertThat(body.getInstructedAmount().getCurrency()).isEqualTo(CURRENCY);
        assertThat(body.getDayOfExecution()).isEqualTo(DayOfExecution._1);
    }

    private FutureSinglePayment buildPeriodicPayment() {
        FutureSinglePayment payment = new FutureSinglePayment();
        payment.setDebtorBankAccount(buildBankAccount());
        payment.setProduct(PaymentProductType.SEPA.getType());
        payment.setAmount(new BigDecimal(AMOUNT_VALUE));
        payment.setReceiver(CREDITOR_NAME);
        payment.setPurpose(INFORMATION);
        payment.setReceiverIban(IBAN);
        payment.setCurrency(CURRENCY);
        payment.setExecutionDate(LocalDate.MIN);
        return payment;
    }

}