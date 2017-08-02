import { BankLoginSettings } from "./BankLoginSettings";
import { BankApi } from "./BankApi";

export interface Bank {
    "bankApi"?: BankApi;
    "bankCode"?: string;
    "bic"?: string;
    "blzHbci"?: string;
    "id"?: string;
    "loginSettings"?: BankLoginSettings;
    "name"?: string;
    "searchIndex"?: Array<string>;
}

