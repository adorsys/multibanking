import { BookingGroup } from "./multibanking/models";

export interface AggregatedGroups {
    "groups"?: BookingGroup[];
    "amount"?: number;
}
