
# TrainCategorizationTransactionData

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**accountTypeId** | **Long** | Identifier of account type.&lt;br/&gt;&lt;br/&gt;1 &#x3D; Checking,&lt;br/&gt;2 &#x3D; Savings,&lt;br/&gt;3 &#x3D; CreditCard,&lt;br/&gt;4 &#x3D; Security,&lt;br/&gt;5 &#x3D; Loan,&lt;br/&gt;6 &#x3D; Pocket (DEPRECATED; will not be returned for any account unless this type has explicitly been set via PATCH),&lt;br/&gt;7 &#x3D; Membership,&lt;br/&gt;8 &#x3D; Bausparen&lt;br/&gt;&lt;br/&gt; | 
**amount** | [**BigDecimal**](BigDecimal.md) | Amount | 
**purpose** | **String** | Purpose. Any symbols are allowed. Maximum length is 2000. Default value: null. |  [optional]
**counterpart** | **String** | Counterpart. Any symbols are allowed. Maximum length is 80. Default value: null. |  [optional]
**counterpartIban** | **String** | Counterpart IBAN. Default value: null. |  [optional]
**counterpartAccountNumber** | **String** | Counterpart account number. Default value: null. |  [optional]
**counterpartBlz** | **String** | Counterpart BLZ. Default value: null. |  [optional]
**counterpartBic** | **String** | Counterpart BIC. Default value: null. |  [optional]
**mcCode** | **String** | Merchant category code (for credit card transactions only). Default value: null. NOTE: This field is currently not regarded. |  [optional]



