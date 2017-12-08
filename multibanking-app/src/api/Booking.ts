import { BankAccount } from "./BankAccount";
import { BookingCategory } from "./BookingCategory";
import { BankApi } from "./BankApi";

export interface Booking {

    "id"?: string;
    "accountId"?: string;
    "additional"?: string;
    "addkey"?: string;
    "amount"?: number;
    "availableHbciBalance"?: number;
    "balance"?: number;
    "bankApi"?: BankApi;
    "bookingCategory"?: BookingCategory;
    "bookingDate"?: Date;
    "chargeValue"?: number;
    "creditHbciBalance"?: number;
    "creditorId"?: string;
    "customerRef"?: string;
    "externalId": string;
    "instRef"?: string;
    "origValue"?: number;
    "otherAccount"?: BankAccount;
    "primanota"?: string;
    "readyHbciBalance"?: number;
    "reversal"?: boolean;
    "sepa"?: boolean;
    "text"?: string;
    "unreadyHbciBalance"?: number;
    "usage"?: string;
    "usedHbciBalance"?: number;
    "standingOrder"?: boolean;
    "userId"?: string;
    "valutaDate"?: Date;
}
