
# EditBankConnectionParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**bankingUserId** | **String** | New online banking user ID. If you do not want to change the current user ID let this field remain unset. In case you need to use finAPI&#39;s web form to let the user update the field, just set the field to any value, so that the service recognizes that you wish to use the web form flow. Note that you cannot clear the current user ID, i.e. a bank connection must always have a user ID (except for when it is a &#39;demo connection&#39;). Max length: 64. |  [optional]
**bankingCustomerId** | **String** | New online banking customer ID. If you do not want to change the current customer ID let this field remain unset. In case you need to use finAPI&#39;s web form to let the user update the field, just set the field to non-empty value, so that the service recognizes that you wish to use the web form flow. If you want to clear the current customer ID, set the field&#39;s value to an empty string (\&quot;\&quot;). Max length: 64. |  [optional]
**bankingPin** | **String** | New online banking PIN. If you do not want to change the current PIN let this field remain unset. In case you need to use finAPI&#39;s web form to let the user update the field, just set the field to non-empty value, so that the service recognizes that you wish to use the web form flow. If you want to clear the current PIN, set the field&#39;s value to an empty string (\&quot;\&quot;).&lt;br/&gt;&lt;br/&gt;NOTE: Before you set this field, please regard the &#39;pinsAreVolatile&#39; flag of this connection&#39;s bank.&lt;br/&gt;Any symbols are allowed. Max length: 170. |  [optional]
**defaultTwoStepProcedureId** | **String** | New default two-step-procedure. Must match the &#39;procedureId&#39; of one of the procedures that are listed in the bank connection. If you do not want to change this field let it remain unset. If you want to clear the current default two-step-procedure, set the field&#39;s value to an empty string (\&quot;\&quot;). |  [optional]
**name** | **String** | New name for the bank connection. Maximum length is 64. If you do not want to change the current name let this field remain unset. If you want to clear the current name, set the field&#39;s value to an empty string (\&quot;\&quot;). |  [optional]



