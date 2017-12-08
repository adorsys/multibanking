export interface Rule {

    "ruleId": string;
    "creditorId": string;
    "expression"?: string;
    "receiver"?: string;
    "mainCategory": string;
    "subCategory"?: string;
    "specification"?: string;
    "taxRelevant"?: boolean;
    "incoming"?: boolean;
    
}
