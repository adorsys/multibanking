import { Contract } from "./Contract";

export interface BookingCategory {
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

