import { BankAccount } from "./BankAccount";
import { BookingCategory } from "./BookingCategory";
import { BankApi } from "./BankApi";

export interface Booking {
    "accountId"?: string;
    /**
     * Additional transation info
     */
    "additional"?: string;
    /**
     * Todo description needed
     */
    "addkey"?: string;
    /**
     * The target aount
     */
    "amount"?: number;
    /**
     * The available balance
     */
    "availableHbciBalance"?: number;
    /**
     * The account balance after this booking
     */
    "balance"?: number;
    /**
     * The origine of this booking
     */
    "bankApi"?: BankApi;
    "bookingCategory"?: BookingCategory;
    /**
     * The booking date
     */
    "bookingDate"?: Date;
    /**
     * The charge value
     */
    "chargeValue"?: number;
    /**
     * The credit balance
     */
    "creditHbciBalance"?: number;
    /**
     * The reference of the opposite party
     */
    "customerRef"?: string;
    /**
     * The external id of this booking
     */
    "externalId": string;
    "id"?: string;
    /**
     * The reference of the corresponding institution
     */
    "instRef"?: string;
    /**
     * The original value
     */
    "origValue"?: number;
    /**
     * The opposite bank account
     */
    "otherAccount"?: BankAccount;
    /**
     * The primanota
     */
    "primanota"?: string;
    /**
     * The ready account balance
     */
    "readyHbciBalance"?: number;
    "reversal"?: boolean;
    "sepa"?: boolean;
    /**
     * Transaction information
     */
    "text"?: string;
    /**
     * The unreleased account balance
     */
    "unreadyHbciBalance"?: number;
    /**
     * The usage of this transaction
     */
    "usage"?: string;
    /**
     * The used balance
     */
    "usedHbciBalance"?: number;
    "userId"?: string;
    /**
     * The value date
     */
    "valutaDate"?: Date;
}
