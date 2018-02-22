import { PaymentType } from "./PaymentType";
import { PaymentChallenge } from "./PaymentChallenge";

export interface Payment {
    "id"?: string;
    "paymentType"?: PaymentType;
    "paymentChallenge"?: PaymentChallenge;
    "receiver": string;
    "receiverBic"?: string;
    "receiverIban"?: string;
    "receiverBankCode"?: string;
    "receiverAccountNumber"?: string;
    "purpose": string;
    "amount": number;
    "executionDay"?: number;
    "firstExecutionDate"?: Date;
    "lastExecutionDate"?: Date;
    "cycle"?: string;

}

