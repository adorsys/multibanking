import { BankAccountBalance } from "./BankAccountBalance";

export interface BankAccount {
    /**
     * The account number
     */
    "accountNumber"?: string;
    "bankAccessId"?: string;
    "bankAccountBalance"?: BankAccountBalance;
    /**
     * The bank name
     */
    "bankName"?: string;
    /**
     * The banc identification code
     */
    "bic"?: string;
    /**
     * The bank code
     */
    "blz"?: string;
    /**
     * The ISO2 Country of this bank account
     */
    "country"?: string;
    /**
     * The currency of this bank account
     */
    "currency"?: string;
    /**
     * The international banc account number
     */
    "iban": string;
    "id"?: string;
    /**
     * The name of this bank account if any
     */
    "name"?: string;
    /**
     * The name of the owner of this account
     */
    "owner"?: string;
    /**
     * The synchronisation status
     */
    "syncStatus"?: BankAccount.SyncStatusEnum;
    /**
     * The type of this bank account
     */
    "type"?: string;
    "userId"?: string;
}

export namespace BankAccount {
    export enum SyncStatusEnum {
        PENDING = <any> 'PENDING',
        SYNC = <any> 'SYNC',
        READY = <any> 'READY'
    }
}
