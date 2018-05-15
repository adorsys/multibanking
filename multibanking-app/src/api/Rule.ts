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
    "taxRelevant"?: boolean;
    "similarityMatchType"?: SimilarityMatchType;
    "incoming"?: boolean;
    "released"?: boolean;

}
