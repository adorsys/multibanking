export interface RuleCategory {
    "id": string;
    "name": string;
    "subcategories": RuleCategory[];
    "specifications": RuleCategory[];
}
