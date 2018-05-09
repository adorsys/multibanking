import { BookingGroup } from "./BookingGroup";

export interface AccountAnalytics {
    "id"?: string;
    "userId"?: string;
    "accountId"?: string;
    "analyticsDate"?: Date;
    "balanceCalculated"?: number;
    "expensesFixed"?: number;
    "expensesNext"?: number;
    "expensesTotal"?: number;
    "incomeFixed"?: number;
    "incomeNext"?: number;
    "incomeTotal"?: number;
    "bookingGroups"?: BookingGroup[];
    
}