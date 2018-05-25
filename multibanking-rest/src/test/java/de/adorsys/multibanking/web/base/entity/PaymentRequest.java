package de.adorsys.multibanking.web.base.entity;

import lombok.Data;

/**
 * Created by peter on 22.05.18 at 08:20.
 */
@Data
public class PaymentRequest {
    private PaymentRequestBody payment = new PaymentRequestBody();
    private String pin;


    @Data
    public static class PaymentRequestBody {
        Integer amount;
        String cycle;
        Integer executionDay;
        String firstExecutionDate;
        String lastExecutionDate;
        PaymentChallenge paymentChallenge;
        String paymentType;
        String purpose;
        String receiver;
        String receiverAccountNumber;
        String receiverBankCode;
        String receiverBic;
        String receiverIban;
        String tanSubmitExternal = "TanSubmitExternal";

    }

    @Data
    public static class PaymentChallenge {
        String data;
        String format;
        String label;
        String title;
    }

    @Data
    public static class TanSubmitExternal {

    }
}
