import { Moment } from 'moment';
import { BookingGroupTO } from './../../multibanking-api/bookingGroupTO';

export interface Budget {
    periodStart: Moment;
    periodEnd: Moment;
    incomeFix: { amount: number, groups: BookingGroupTO[] };
    incomeOther: { amount: number, groups: BookingGroupTO[] };
    expensesFix: { amount: number, groups: BookingGroupTO[] };
    expensesVariable: { amount: number, groups: BookingGroupTO[] };
    expensesOther: { amount: number, groups: BookingGroupTO[] };
}
