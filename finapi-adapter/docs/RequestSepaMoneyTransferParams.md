
# RequestSepaMoneyTransferParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**recipientName** | **String** | Name of the recipient. Note: Neither finAPI nor the involved bank servers are guaranteed to validate the recipient name. Even if the recipient name does not depict the actual registered account holder of the specified recipient account, the money transfer request might still be successful. | 
**recipientIban** | **String** | IBAN of the recipient&#39;s account | 
**recipientBic** | **String** | BIC of the recipient&#39;s account. Note: This field is optional if - and only if - the bank connection of the account that you want to transfer money from supports the IBAN-Only money transfer. You can find this out via GET /bankConnections/&lt;id&gt;. Also note that when a BIC is given, then this BIC will be used for the money transfer request independent of whether it is required or not. |  [optional]
**amount** | [**BigDecimal**](BigDecimal.md) | The amount to transfer. Must be a positive decimal number with at most two decimal places (e.g. 99.90) | 
**purpose** | **String** | The purpose of the transfer transaction |  [optional]
**accountId** | **Long** | Identifier of the bank account that you want to transfer money from | 
**bankingPin** | **String** | Online banking PIN. If a PIN is stored in the account&#39;s bank connection, then this field may remain unset. If the field is set though then it will always be used (even if there is some other PIN stored in the bank connection). |  [optional]
**twoStepProcedureId** | **String** | The bank-given ID of the two-step-procedure that should be used for the money transfer. For a list of available two-step-procedures, see the corresponding bank connection (GET /bankConnections). If this field is not set, then the bank connection&#39;s default two-step procedure will be used. Note that in this case, when the bank connection has no default two-step procedure set, then the service will return an error (see response messages for details). |  [optional]
**additionalMoneyTransfers** | [**List&lt;Apiv1accountsrequestSepaMoneyTransferAdditionalMoneyTransfers&gt;**](Apiv1accountsrequestSepaMoneyTransferAdditionalMoneyTransfers.md) | In case that you want to submit not just a single money transfer, but instead do a collective money transfer, use this field to pass a list of additional money transfer orders. The service will then pass a collective money transfer request to the bank, including both the money transfer specified on the top-level, as well as all money transfers specified in this list. |  [optional]



