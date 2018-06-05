import { SimilarityMatchType } from "./SimilarityMatchType";

export interface Rule {
    "id"?: string;
    "ruleId"?: string;
    "creditorId": string;
    "receiver"?: string;
    "expression"?: string
    "mainCategory"?: string;
    "subCategory"?: string;
    "specification"?: string;
    "similarityMatchType"?: SimilarityMatchType;
    "incoming"?: boolean;
    "custom"?: Map<string, string>;

}
