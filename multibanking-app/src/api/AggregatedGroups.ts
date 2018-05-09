import { BookingGroup } from "./BookingGroup";

export interface AggregatedGroups {
    "groups"?: BookingGroup[];
    "amount"?: number;
}