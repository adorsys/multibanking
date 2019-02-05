
# BankConnection

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Bank connection identifier | 
**bankId** | **Long** | Identifier of the bank that this connection belongs to. NOTE: This field is DEPRECATED and will get removed at some point. Please refer to the &#39;bank&#39; field instead. | 
**bank** | [**Bank**](Bank.md) | Bank that this connection belongs to | 
**name** | **String** | Custom name for the bank connection. You can set this field with the &#39;Edit a bank connection&#39; service, as well as during the initial import of the bank connection. Maximum length is 64. |  [optional]
**bankingUserId** | **String** | Stored online banking user ID credential. This field may be null for the &#39;demo connection&#39;. Note that if your client has no license for processing banking credentials then a banking user ID will always be &#39;XXXXX&#39;. |  [optional]
**bankingCustomerId** | **String** | Stored online banking customer ID credential. Note that if your client has no license for processing banking credentials or if this field contains a value that requires password protection (see field ‘isCustomerIdPassword’ in Bank Resource) then the banking customer ID will always be &#39;XXXXX&#39;. |  [optional]
**bankingPin** | **String** | Stored online banking PIN. If a PIN is stored, this will always be &#39;XXXXX&#39;. |  [optional]
**type** | [**TypeEnum**](#TypeEnum) | Bank connection type | 
**updateStatus** | [**UpdateStatusEnum**](#UpdateStatusEnum) | Current status of transactions download. The POST /bankConnections/import and POST /bankConnections/&lt;id&gt;/update services will set this flag to IN_PROGRESS before they return. Once the import or update has finished, the status will be changed to READY. | 
**categorizationStatus** | [**CategorizationStatusEnum**](#CategorizationStatusEnum) | Current status of transactions categorization. The asynchronous download process that is triggered by a call of the POST /bankConnections/import and POST /bankConnections/&lt;id&gt;/update services (and also by finAPI&#39;s auto update, if enabled) will set this flag to PENDING once the download has finished and a categorization is scheduled for the imported transactions. A separate categorization thread will then start to categorize the transactions (during this process, the status is IN_PROGRESS). When categorization has finished, the status will be (re-)set to READY. Note that the current categorization status should only be queried after the download has finished, i.e. once the download status has switched from IN_PROGRESS to READY. | 
**lastManualUpdate** | [**UpdateResult**](UpdateResult.md) | Result of the last manual update of this bank connection. If no manual update has ever been done so far, then this field will not be set. |  [optional]
**lastAutoUpdate** | [**UpdateResult**](UpdateResult.md) | Result of the last auto update of this bank connection (ran by finAPI&#39;s automatic batch update process). If no auto update has ever been done so far, then this field will not be set. |  [optional]
**twoStepProcedures** | [**List&lt;TwoStepProcedure&gt;**](TwoStepProcedure.md) | Available two-step-procedures for this bank connection, used for submitting a money transfer or direct debit request (see /accounts/requestSepaMoneyTransfer or /requestSepaDirectDebit). The available two-step-procedures are re-evaluated each time this bank connection is updated (/bankConnections/update). This means that this list may change as a result of an update. |  [optional]
**ibanOnlyMoneyTransferSupported** | **Boolean** | Whether this bank connection accepts money transfer requests where the recipient&#39;s account is defined just by the IBAN (without an additional BIC). This field is re-evaluated each time this bank connection is updated. See also: /accounts/requestSepaMoneyTransfer | 
**ibanOnlyDirectDebitSupported** | **Boolean** | Whether this bank connection accepts direct debit requests where the debitor&#39;s account is defined just by the IBAN (without an additional BIC). This field is re-evaluated each time this bank connection is updated. See also: /accounts/requestSepaDirectDebit | 
**collectiveMoneyTransferSupported** | **Boolean** | &lt;b&gt;DEPRECATED! DO NOT USE THIS FIELD, AS IT IS UNRELIABLE. INSTEAD, REFER TO THE &#39;supportedOrders&#39; FIELD IN THE ACCOUNT RESOURCE.&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;Whether this bank connection supports submitting collective money transfers. This field is re-evaluated each time this bank connection is updated. See also: /accounts/requestSepaMoneyTransfer | 
**defaultTwoStepProcedureId** | **String** | The default two-step-procedure. Must match one of the available &#39;procedureId&#39;s from the &#39;twoStepProcedures&#39; list. When this field is set, you can execute two-step-procedures (accounts/requestSepaMoneyTransfer or /requestSepaDirectDebit) without having to explicitly set a procedure. finAPI will use the default procedure in such cases. Note that the list of available procedures of a bank connection may change as a result of an update of the connection, and if this field references a procedure that is no longer available after an update, finAPI will automatically clear the default procedure (set it to null). |  [optional]
**accountIds** | **List&lt;Long&gt;** | Identifiers of the accounts that belong to this bank connection | 
**owners** | [**List&lt;BankConnectionOwner&gt;**](BankConnectionOwner.md) | Information about the owner(s) of the bank connection |  [optional]


<a name="TypeEnum"></a>
## Enum: TypeEnum
Name | Value
---- | -----
ONLINE | &quot;ONLINE&quot;
DEMO | &quot;DEMO&quot;


<a name="UpdateStatusEnum"></a>
## Enum: UpdateStatusEnum
Name | Value
---- | -----
IN_PROGRESS | &quot;IN_PROGRESS&quot;
READY | &quot;READY&quot;


<a name="CategorizationStatusEnum"></a>
## Enum: CategorizationStatusEnum
Name | Value
---- | -----
IN_PROGRESS | &quot;IN_PROGRESS&quot;
PENDING | &quot;PENDING&quot;
READY | &quot;READY&quot;



