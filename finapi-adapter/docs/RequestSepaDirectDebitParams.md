
# RequestSepaDirectDebitParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**accountId** | **Long** | Identifier of the bank account to which you want to transfer the money. | 
**bankingPin** | **String** | Online banking PIN. Any symbols are allowed. Max length: 170. If a PIN is stored in the bank connection, then this field may remain unset. If finAPI&#39;s web form is not required and the field is set though then it will always be used (even if there is some other PIN stored in the bank connection). If you want the user to enter a PIN in finAPI&#39;s web form even when a PIN is stored, then just set the field to any value, so that the service recognizes that you wish to use the web form flow. |  [optional]
**storePin** | **Boolean** | Whether to store the PIN. If the PIN is stored, it is not required to pass the PIN again when executing SEPA orders. Default value is &#39;false&#39;. &lt;br/&gt;&lt;br/&gt;NOTES:&lt;br/&gt; - before you set this field to true, please regard the &#39;pinsAreVolatile&#39; flag of the bank connection that the account belongs to;&lt;br/&gt; - this field is ignored in case when the user will need to use finAPI&#39;s web form. The user will be able to decide whether to store the PIN or not in the web form, depending on the &#39;pinStorageAvailableInWebForm&#39; setting (see Client Configuration). |  [optional]
**twoStepProcedureId** | **String** | The bank-given ID of the two-step-procedure that should be used for the order. For a list of available two-step-procedures, see the corresponding bank connection (GET /bankConnections). If this field is not set, then the bank connection&#39;s default two-step-procedure will be used. Note that in this case, when the bank connection has no default two-step-procedure set, then the response of the service depends on whether you need to use finAPI&#39;s web form or not. If you need to use the web form, the user will be prompted to select the two-step-procedure within the web form. If you don&#39;t need to use the web form, then the service will return an error (passing a value for this field is required in this case). |  [optional]
**directDebitType** | [**DirectDebitTypeEnum**](#DirectDebitTypeEnum) | Type of the direct debit; either &lt;code&gt;BASIC&lt;/code&gt; or &lt;code&gt;B2B&lt;/code&gt; (Business-To-Business). Please note that an account which supports the basic type must not necessarily support B2B (or vice versa). Check the source account&#39;s &#39;supportedOrders&#39; field to find out which types of direct debit it supports.&lt;br/&gt;&lt;br/&gt; | 
**sequenceType** | [**SequenceTypeEnum**](#SequenceTypeEnum) | Sequence type of the direct debit. Possible values:&lt;br/&gt;&lt;br/&gt;&amp;bull; &lt;code&gt;OOFF&lt;/code&gt; - means that this is a one-time direct debit order&lt;br/&gt;&amp;bull; &lt;code&gt;FRST&lt;/code&gt; - means that this is the first in a row of multiple direct debit orders&lt;br/&gt;&amp;bull; &lt;code&gt;RCUR&lt;/code&gt; - means that this is one (but not the first or final) within a row of multiple direct debit orders&lt;br/&gt;&amp;bull; &lt;code&gt;FNAL&lt;/code&gt; - means that this is the final in a row of multiple direct debit orders&lt;br/&gt;&lt;br/&gt; | 
**executionDate** | **String** | Execution date for the direct debit(s), in the format &#39;YYYY-MM-DD&#39;. | 
**singleBooking** | **Boolean** | This field is only regarded when you pass multiple orders. It determines whether the orders should be processed by the bank as one collective booking (in case of &#39;false&#39;), or as single bookings (in case of &#39;true&#39;). Default value is &#39;false&#39;. |  [optional]
**directDebits** | [**List&lt;SingleDirectDebitData&gt;**](SingleDirectDebitData.md) | List of the direct debits that you want to execute (may contain at most 15000 items). Please check the account&#39;s &#39;supportedOrders&#39; field to find out whether you can pass multiple direct debits or just one. | 


<a name="DirectDebitTypeEnum"></a>
## Enum: DirectDebitTypeEnum
Name | Value
---- | -----
B2B | &quot;B2B&quot;
BASIC | &quot;BASIC&quot;


<a name="SequenceTypeEnum"></a>
## Enum: SequenceTypeEnum
Name | Value
---- | -----
OOFF | &quot;OOFF&quot;
FRST | &quot;FRST&quot;
RCUR | &quot;RCUR&quot;
FNAL | &quot;FNAL&quot;



