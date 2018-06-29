import { BookingGroup } from "./BookingGroup";
import { Moment } from "moment";

export interface Budget {
    periodStart: Moment,
    periodEnd: Moment,
    incomeFix: { amount: number, groups: BookingGroup[] };
    incomeOther: { amount: number, groups: BookingGroup[] };
    expensesFix: { amount: number, groups: BookingGroup[] };
    expensesVariable: { amount: number, groups: BookingGroup[] };
    expensesOther: { amount: number, groups: BookingGroup[] };
}