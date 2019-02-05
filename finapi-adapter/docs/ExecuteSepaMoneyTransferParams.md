
# ExecuteSepaMoneyTransferParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**accountId** | **Long** | Identifier of the bank account that you want to transfer money from | 
**bankingTan** | **String** | Banking TAN that the user received from the bank for executing the money transfer order. The field is required if you are licensed to perform SEPA money transfers yourself. Otherwise, i.e. when finAPI&#39;s web form flow is required, the web form will deal with executing the service itself. |  [optional]



