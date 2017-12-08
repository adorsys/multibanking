import { Contract } from "./Contract";

export interface BookingCategory {
    "contract"?: Contract;
    "mainCategory"?: string;
    "specification"?: string;
    "subCategory"?: string;
    "variable"?: boolean;
    "rules"?: string[];
}

