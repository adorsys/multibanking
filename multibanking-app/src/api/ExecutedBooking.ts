import { Booking } from "./Booking";

export interface ExecutedBooking {
    "bookingId": string;
    "executionDate": Date;
    "executed": boolean;
    "loadedBooking": Booking;
}