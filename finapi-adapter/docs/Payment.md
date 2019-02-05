
# Payment

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Payment identifier | 
**accountId** | **Long** | Identifier of the account to which this payment relates | 
**requestDate** | **String** | Time of when this payment was requested, in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time) | 
**executionDate** | **String** | Time of when this payment was executed, in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time) |  [optional]
**type** | [**TypeEnum**](#TypeEnum) | Payment type | 
**status** | [**StatusEnum**](#StatusEnum) | Current payment status:&lt;br/&gt; &amp;bull; PENDING: means that this payment has been requested, but not yet executed.&lt;br/&gt; &amp;bull; SUCCESSFUL: means that this payment has been successfully executed.&lt;br/&gt; &amp;bull; NOT_SUCCESSFUL: means that this payment could not be executed successfully.&lt;br/&gt; &amp;bull; DISCARDED: means that this payment was discarded because another payment was requested for the same account before this payment was executed. | 
**bankMessage** | **String** | Contains the bank&#39;s response to the execution of this payment. This field is not set until the payment gets executed. Note that even after the execution of the payment, the field might still not be set, if the bank did not send any response message. |  [optional]
**amount** | [**BigDecimal**](BigDecimal.md) | Total money amount of the payment order(s), as absolute value | 
**orderCount** | **Integer** | Total count of orders included in this payment | 


<a name="TypeEnum"></a>
## Enum: TypeEnum
Name | Value
---- | -----
MONEY_TRANSFER | &quot;MONEY_TRANSFER&quot;
DIRECT_DEBIT | &quot;DIRECT_DEBIT&quot;


<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
PENDING | &quot;PENDING&quot;
SUCCESSFUL | &quot;SUCCESSFUL&quot;
NOT_SUCCESSFUL | &quot;NOT_SUCCESSFUL&quot;
DISCARDED | &quot;DISCARDED&quot;



