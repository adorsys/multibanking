
# NotificationRuleParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**triggerEvent** | [**TriggerEventEnum**](#TriggerEventEnum) | Trigger event type | 
**params** | **Map&lt;String, String&gt;** | Additional parameters that are specific to the chosen trigger event type. Please refer to the documentation for details. |  [optional]
**callbackHandle** | **String** | An arbitrary string that finAPI will include into the notifications that it sends based on this rule and that you can use to identify the notification in your application. For instance, you could include the identifier of the user that you create this rule for. Maximum allowed length of the string is 512 characters.&lt;br/&gt;&lt;br/&gt;Note that for this parameter, you can pass the symbols &#39;/&#39;, &#39;&#x3D;&#39;, &#39;%&#39; and &#39;\&quot;&#39; in addition to the symbols that are generally allowed in finAPI (see https://finapi.zendesk.com/hc/en-us/articles/222013148). This was done to enable you to set Base64 encoded strings and JSON structures for the callback handle. |  [optional]
**includeDetails** | **Boolean** | Whether the notification messages that will be sent based on this rule should contain encrypted detailed data or not |  [optional]


<a name="TriggerEventEnum"></a>
## Enum: TriggerEventEnum
Name | Value
---- | -----
NEW_ACCOUNT_BALANCE | &quot;NEW_ACCOUNT_BALANCE&quot;
NEW_TRANSACTIONS | &quot;NEW_TRANSACTIONS&quot;
BANK_LOGIN_ERROR | &quot;BANK_LOGIN_ERROR&quot;
FOREIGN_MONEY_TRANSFER | &quot;FOREIGN_MONEY_TRANSFER&quot;
LOW_ACCOUNT_BALANCE | &quot;LOW_ACCOUNT_BALANCE&quot;
HIGH_TRANSACTION_AMOUNT | &quot;HIGH_TRANSACTION_AMOUNT&quot;
CATEGORY_CASH_FLOW | &quot;CATEGORY_CASH_FLOW&quot;
NEW_TERMS_AND_CONDITIONS | &quot;NEW_TERMS_AND_CONDITIONS&quot;



