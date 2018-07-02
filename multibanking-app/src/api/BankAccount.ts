import { BankAccountBalance } from "./BankAccountBalance";

export interface BankAccount {
    
    "id"?: string;
    "accountNumber"?: string;
    "bankAccessId"?: string;
    "bankAccountBalance"?: BankAccountBalance;
    "bankName"?: string;
    "lastSync"?: Date;
    "bic"?: string;
    "blz"?: string;
    "country"?: string;
    "currency"?: string;
    "iban": string;
    "name"?: string;
    "owner"?: string;
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
