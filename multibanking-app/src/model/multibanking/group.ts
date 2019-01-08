/**
 * Multibanking REST Api
 * Use a bank code (blz) ending with X00 000 00 like 300 000 00 to run aggainst the mock backend. Find the mock backend at ${hostname}:10010
 *
 * OpenAPI spec version: 4.1.2-SNAPSHOT
 * Contact: age@adorsys.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


export interface Group {
    blacklistMatcher?: Array<string>;
    name?: string;
    type?: Group.TypeEnum;
    whitelistMatcher?: Array<string>;
}
export namespace Group {
    export type TypeEnum = 'STANDING_ORDER' | 'RECURRENT_INCOME' | 'RECURRENT_SEPA' | 'RECURRENT_NONSEPA' | 'CUSTOM' | 'OTHER_INCOME' | 'OTHER_EXPENSES';
    export const TypeEnum = {
        STANDINGORDER: 'STANDING_ORDER' as TypeEnum,
        RECURRENTINCOME: 'RECURRENT_INCOME' as TypeEnum,
        RECURRENTSEPA: 'RECURRENT_SEPA' as TypeEnum,
        RECURRENTNONSEPA: 'RECURRENT_NONSEPA' as TypeEnum,
        CUSTOM: 'CUSTOM' as TypeEnum,
        OTHERINCOME: 'OTHER_INCOME' as TypeEnum,
        OTHEREXPENSES: 'OTHER_EXPENSES' as TypeEnum
    }
}