import { Contract } from "./Contract";
import { GroupType } from "./GroupType";
import { BookingPeriod } from "./BookingPeriod";

export interface BookingGroup {
    "type"?: GroupType;
    "mainCategory"?: string;
    "subCategory"?: string;
    "specification"?: string;
    "otherAccount"?: string;
    "amount"?: number;
    "bookingPeriods"?: BookingPeriod[];
    "contract"?: Contract;
}