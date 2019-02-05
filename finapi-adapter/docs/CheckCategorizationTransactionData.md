
# CheckCategorizationTransactionData

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**transactionId** | **String** | Identifier of transaction. This can be any arbitrary string that will be passed back in the response so that you can map the results to the given transactions. Note that the identifier must be unique within the given list of transactions. | 
**accountTypeId** | **Long** | Identifier of account type.&lt;br/&gt;&lt;br/&gt;1 &#x3D; Checking,&lt;br/&gt;2 &#x3D; Savings,&lt;br/&gt;3 &#x3D; CreditCard,&lt;br/&gt;4 &#x3D; Security,&lt;br/&gt;5 &#x3D; Loan,&lt;br/&gt;6 &#x3D; Pocket (DEPRECATED; will not be returned for any account unless this type has explicitly been set via PATCH),&lt;br/&gt;7 &#x3D; Membership,&lt;br/&gt;8 &#x3D; Bausparen&lt;br/&gt;&lt;br/&gt; | 
**amount** | [**BigDecimal**](BigDecimal.md) | Amount | 
**purpose** | **String** | Purpose. Any symbols are allowed. Maximum length is 2000. Default value: null. |  [optional]
**counterpart** | **String** | Counterpart. Any symbols are allowed. Maximum length is 80. Default value: null. |  [optional]
**counterpartIban** | **String** | Counterpart IBAN. Default value: null. |  [optional]
**counterpartAccountNumber** | **String** | Counterpart account number. Default value: null. |  [optional]
**counterpartBlz** | **String** | Counterpart BLZ. Default value: null. |  [optional]
**counterpartBic** | **String** | Counterpart BIC. Default value: null. |  [optional]
**mcCode** | **String** | Merchant category code (for credit card transactions only). May only contain up to 4 digits. Default value: null. |  [optional]
**typeCodeZka** | **String** | ZKA business transaction code which relates to the transaction&#39;s type (Number from 0 through 999). Default value: null. |  [optional]



