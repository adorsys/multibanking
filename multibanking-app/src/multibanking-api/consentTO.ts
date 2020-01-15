/**
 * Multibanking REST Api
 * Use a bank code (blz) ending with X00 000 00 like 300 000 00 to run aggainst the mock backend. Find the mock backend at ${hostname}:10010
 *
 * OpenAPI spec version: 5.4.5-SNAPSHOT
 * Contact: age@adorsys.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
import { AccountReferenceTO } from './accountReferenceTO';


export interface ConsentTO {

    temporary?: boolean;

    /**
     * consent accounts for details
     */
    accounts?: Array<AccountReferenceTO>;
    /**
     * consent accounts for balances
     */
    balances?: Array<AccountReferenceTO>;
    /**
     * allowed access frequency per day
     */
    frequencyPerDay?: number;
    /**
     * Consent id
     */
    id?: string;
    /**
     * account iban
     */
    psuAccountIban: string;
    /**
     * recurring indicator
     */
    recurringIndicator?: boolean;
    /**
     * Consent redirect id
     */
    redirectId?: string;
    /**
     * URI of the TPP, where the transaction flow shall be redirected to after a Redirect.                  Mandated for the Redirect SCA Approach, specifically          when TPP-Redirect-Preferred equals \"true\".         It is recommended to always use this header field.
     */
    tppRedirectUri?: string;
    /**
     * consent accounts for transactions
     */
    transactions?: Array<AccountReferenceTO>;
    /**
     * consent valid date
     */
    validUntil?: string;
}
