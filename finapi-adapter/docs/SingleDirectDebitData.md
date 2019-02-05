
# SingleDirectDebitData

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**debitorName** | **String** | Name of the debitor. Note: Neither finAPI nor the involved bank servers are guaranteed to validate the debitor name. Even if the debitor name does not depict the actual registered account holder of the specified debitor account, the direct debit request might still be successful. | 
**debitorIban** | **String** | IBAN of the debitor&#39;s account | 
**debitorBic** | **String** | BIC of the debitor&#39;s account. Note: This field is optional if - and only if - the bank connection of the account that you want to transfer money to supports the IBAN-Only direct debit. You can find this out via GET /bankConnections/&lt;id&gt;. Also note that when a BIC is given, then this BIC will be used for the direct debit request independent of whether it is required or not. |  [optional]
**amount** | [**BigDecimal**](BigDecimal.md) | The amount to transfer. Must be a positive decimal number with at most two decimal places (e.g. 99.99) | 
**purpose** | **String** | The purpose of the transfer transaction |  [optional]
**sepaPurposeCode** | **String** | SEPA purpose code, according to ISO 20022, external codes set. |  [optional]
**mandateId** | **String** | Mandate ID that this direct debit order is based on. | 
**mandateDate** | **String** | Date of the mandate that this direct debit order is based on, in the format &#39;YYYY-MM-DD&#39; | 
**creditorId** | **String** | Creditor ID of the source account&#39;s holder |  [optional]
**endToEndId** | **String** | End-To-End ID for the transfer transaction |  [optional]



