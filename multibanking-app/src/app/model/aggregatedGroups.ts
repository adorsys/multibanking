import { BookingGroupTO } from './../../multibanking-api/bookingGroupTO';

export interface AggregatedGroups {
    groups?: BookingGroupTO[];
    amount?: number;
}
