import { BookingGroup } from "./BookingGroup";

export interface Budget {
    incomeFix: { amount: number, groups: BookingGroup[] };
    incomeOther: { amount: number, groups: BookingGroup[] };
    expensesFix: { amount: number, groups: BookingGroup[] };
    expensesVariable: { amount: number, groups: BookingGroup[] };
    expensesOther: { amount: number, groups: BookingGroup[] };
}