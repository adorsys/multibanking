import { Contract } from "./Contract";

export interface BookingGroup {
    "variable"?: boolean;
    "mainCategory"?: string;
    "subCategory"?: string;
    "specification"?: string;
    "otherAccount"?: string;
    "amount"?: number;
    "nextExecutionDate"?: Date;
    "contract"?: Contract;
}