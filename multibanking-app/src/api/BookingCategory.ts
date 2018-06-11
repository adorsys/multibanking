import { Contract } from "./Contract";

export interface BookingCategory {
    "receiver"?: string;
    "mainCategory"?: string;
    "specification"?: string;
    "subCategory"?: string;
    "custom"?: Map<string, string>;
    "email"?: string;
    "homepage"?: string;
    "hotline"?: string;
    "interval"?: Contract.IntervalEnum;
    "logo"?: string;
    "rules"?: string[];
}

