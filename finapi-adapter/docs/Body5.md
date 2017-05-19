
# Body5

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**bankingUserId** | **String** | New online banking user ID. If you do not want to change the current user ID let this field remain unset. Note that you cannot clear the current user ID, i.e. a bank connection must always have a user ID (except for when it is a &#39;demo connection&#39;). |  [optional]
**bankingCustomerId** | **String** | New online banking customer ID. If you do not want to change the current customer ID let this field remain unset. If you want to clear the current customer ID, set the field&#39;s value to an empty string (\&quot;\&quot;). |  [optional]
**bankingPin** | **String** | New online banking PIN. If you do not want to change the current PIN let this field remain unset. If you want to clear the current PIN, set the field&#39;s value to an empty string (\&quot;\&quot;). |  [optional]
**defaultTwoStepProcedureId** | **String** | New default two-step-procedure. Must match the &#39;procedureId&#39; of one of the procedures that are listed in the bank connection. If you do not want to change this field let it remain unset. If you want to clear the current default two-step-procedure, set the field&#39;s value to an empty string (\&quot;\&quot;). |  [optional]
**name** | **String** | New name for the bank connection. Maximum length is 64. If you do not want to change the current name let this field remain unset. If you want to clear the current name, set the field&#39;s value to an empty string (\&quot;\&quot;). |  [optional]



