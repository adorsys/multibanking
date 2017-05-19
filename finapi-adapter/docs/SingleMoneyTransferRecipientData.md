
# SingleMoneyTransferRecipientData

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**recipientName** | **String** | Name of the recipient. Note: Neither finAPI nor the involved bank servers are guaranteed to validate the recipient name. Even if the recipient name does not depict the actual registered account holder of the specified recipient account, the money transfer request might still be successful. | 
**recipientIban** | **String** | IBAN of the recipient&#39;s account | 
**recipientBic** | **String** | BIC of the recipient&#39;s account. Note: This field is optional if - and only if - the bank connection of the account that you want to transfer money from supports the IBAN-Only money transfer. You can find this out via GET /bankConnections/&lt;id&gt;. Also note that when a BIC is given, then this BIC will be used for the money transfer request independent of whether it is required or not. |  [optional]
**amount** | [**BigDecimal**](BigDecimal.md) | The amount to transfer. Must be a positive decimal number with at most two decimal places (e.g. 99.90) | 
**purpose** | **String** | The purpose of the transfer transaction |  [optional]



