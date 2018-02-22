import { Payment } from "./Payment";

export interface PaymentCreate {
    "payment": Payment;
    "pin"?: string;
}

