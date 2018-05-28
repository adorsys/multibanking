import { ExecutedBooking } from "./ExecutedBooking";

export interface BookingPeriod {
    "start"?: string;
    "end"?: string;
    "amount"?: number;
    "bookings"?: ExecutedBooking[];
}