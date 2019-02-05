
# NotificationRule

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Notification rule identifier | 
**triggerEvent** | [**TriggerEventEnum**](#TriggerEventEnum) | Trigger event type | 
**params** | **Map&lt;String, String&gt;** | Additional parameters that are specific to the trigger event type. Please refer to the documentation for details. |  [optional]
**callbackHandle** | **String** | The string that finAPI includes into the notifications that it sends based on this rule. |  [optional]
**includeDetails** | **Boolean** | Whether the notification messages that will be sent based on this rule contain encrypted detailed data or not | 


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



