
# Transaction

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Transaction identifier | 
**parentId** | **Long** | Parent transaction identifier |  [optional]
**accountId** | **Long** | Account identifier | 
**valueDate** | **String** | Value date in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). | 
**bankBookingDate** | **String** | Bank booking date in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). | 
**finapiBookingDate** | **String** | finAPI Booking date in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). NOTE: In some cases, banks may deliver transactions that are booked in future, but already included in the current account balance. To keep the account balance consistent with the set of transactions, such \&quot;future transactions\&quot; will be imported with their finapiBookingDate set to the current date (i.e.: date of import). The finapiBookingDate will automatically get adjusted towards the bankBookingDate each time the associated bank account is updated. Example: A transaction is imported on July, 3rd, with a bank reported booking date of July, 6th. The transaction will be imported with its finapiBookingDate set to July, 3rd. Then, on July 4th, the associated account is updated. During this update, the transaction&#39;s finapiBookingDate will be automatically adjusted to July 4th. This adjustment of the finapiBookingDate takes place on each update until the bank account is updated on July 6th or later, in which case the transaction&#39;s finapiBookingDate will be adjusted to its final value, July 6th.&lt;br/&gt; The finapiBookingDate is the date that is used by the finAPI PFM services. E.g. when you calculate the spendings of an account for the current month, and have a transaction with finapiBookingDate in the current month but bankBookingDate at the beginning of the next month, then this transaction is included in the calculations (as the bank has this transaction&#39;s amount included in the current account balance as well). | 
**amount** | [**BigDecimal**](BigDecimal.md) | Transaction amount | 
**purpose** | **String** | Transaction purpose. Maximum length: 2000 |  [optional]
**counterpartName** | **String** | Counterpart name. Maximum length: 80 |  [optional]
**counterpartAccountNumber** | **String** | Counterpart account number |  [optional]
**counterpartIban** | **String** | Counterpart IBAN |  [optional]
**counterpartBlz** | **String** | Counterpart BLZ |  [optional]
**counterpartBic** | **String** | Counterpart BIC |  [optional]
**counterpartBankName** | **String** | Counterpart Bank name |  [optional]
**counterpartMandateReference** | **String** | The mandate reference of the counterpart |  [optional]
**counterpartCustomerReference** | **String** | The customer reference of the counterpart |  [optional]
**counterpartCreditorId** | **String** | The creditor ID of the counterpart |  [optional]
**type** | **String** | Transaction type, according to the bank. If set, this will contain a German term that you can display to the user. Some examples of common values are: \&quot;Lastschrift\&quot;, \&quot;Auslands&amp;uuml;berweisung\&quot;, \&quot;Geb&amp;uuml;hren\&quot;, \&quot;Zinsen\&quot;. The maximum possible length of this field is 255 characters. |  [optional]
**typeCodeZka** | **String** | ZKA business transaction code which relates to the transaction&#39;s type. Possible values range from 1 through 999. If no information about the ZKA type code is available, then this field will be null. |  [optional]
**typeCodeSwift** | **String** | SWIFT transaction type code. If no information about the SWIFT code is available, then this field will be null. |  [optional]
**sepaPurposeCode** | **String** | SEPA purpose code, according to ISO 20022 |  [optional]
**primanota** | **String** | Transaction primanota (bank side identification number) |  [optional]
**category** | [**Category**](Category.md) | Transaction category, if any is assigned. Note: Recently imported transactions that have currently no category assigned might still get categorized by the background categorization process. To check the status of the background categorization, see GET /bankConnections. Manual category assignments to a transaction will remove the transaction from the background categorization process (i.e. the background categorization process will never overwrite a manual category assignment). |  [optional]
**labels** | [**List&lt;Label&gt;**](Label.md) | Array of assigned labels |  [optional]
**isPotentialDuplicate** | **Boolean** | While finAPI uses a well-elaborated algorithm for uniquely identifying transactions, there is still the possibility that during an account update, a transaction that was imported previously may be imported a second time as a new transaction. For example, this can happen if some transaction data changes on the bank server side. However, finAPI also includes an algorithm of identifying such \&quot;potential duplicate\&quot; transactions. If this field is set to true, it means that finAPI detected a similar transaction that might actually be the same. It is recommended to communicate this information to the end user, and give him an option to delete the transaction in case he confirms that it really is a duplicate. | 
**isAdjustingEntry** | **Boolean** | Indicating whether this transaction is an adjusting entry (&#39;Zwischensaldo&#39;).&lt;br/&gt;&lt;br/&gt;Adjusting entries do not originate from the bank, but are added by finAPI during an account update when the bank reported account balance does not add up to the set of transactions that finAPI receives for the account. In this case, the adjusting entry will fix the deviation between the balance and the received transactions so that both adds up again.&lt;br/&gt;&lt;br/&gt;Possible causes for such deviations are:&lt;br/&gt;- Inconsistencies in how the bank calculates the balance, for instance when not yet booked transactions are already included in the balance, but not included in the set of transactions&lt;br/&gt;- Gaps in the transaction history that finAPI receives, for instance because the account has not been updated for a while and older transactions are no longer available | 
**isNew** | **Boolean** | Indicating whether this transaction is &#39;new&#39; or not. Any newly imported transaction will have this flag initially set to true. How you use this field is up to your interpretation. For example, you might want to set it to false once a user has clicked on/seen the transaction. You can change this flag to &#39;false&#39; with the PATCH method. | 
**importDate** | **String** | Date of transaction import, in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). | 
**children** | **List&lt;Long&gt;** | Sub-transactions identifiers (if this transaction is split) |  [optional]
**paypalData** | [**PaypalTransactionData**](PaypalTransactionData.md) | Additional, PayPal-specific transaction data. This field is only set for transactions that belong to an account of the &#39;PayPal&#39; bank (BLZ &#39;PAYPAL&#39;). |  [optional]
**endToEndReference** | **String** | End-To-End reference |  [optional]



