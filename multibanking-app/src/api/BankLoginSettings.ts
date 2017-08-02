import { BankLoginCredential } from "./BankLoginCredential";

export interface BankLoginSettings {
    "additionalIcons"?: { [key: string]: string; };
    "advice"?: string;
    "authType"?: string;
    "credentials"?: Array<BankLoginCredential>;
    "icon"?: string;
}

