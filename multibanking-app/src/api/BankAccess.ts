export interface BankAccess {
    /**
     * The bank code
     */
    "bankCode": string;
    /**
     * The bank login
     */
    "bankLogin": string;
    /**
     * The 2nd bank login id
     */
    "bankLogin2"?: string;
    /**
     * The bank name
     */
    "bankName"?: string;
    "categorizeBookings"?: boolean;
    "id"?: string;
    "pin"?: string;
    "pin2"?: string;
    "storeAnalytics"?: boolean;
    "storeBookings"?: boolean;
    "storePin"?: boolean;
    "temporary"?: boolean;
    "userId"?: string;
}

