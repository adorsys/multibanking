
# MockBankConnectionUpdate

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**bankConnectionId** | **Long** | Bank connection identifier | 
**simulateBankLoginError** | **Boolean** | Whether to simulate the case that the update fails due to incorrect banking credentials. Note that there is no real communication to any bank server involved, so you won&#39;t lock your accounts when enabling this flag. Default value is &#39;false&#39;. |  [optional]
**mockAccountsData** | [**List&lt;Apiv1testsmockBatchUpdateMockAccountsData&gt;**](Apiv1testsmockBatchUpdateMockAccountsData.md) | Mock accounts data. Note that for accounts that exist in a bank connection but that you do not specify in this list, the service will act like those accounts are not received by the bank servers. This means that any accounts that you do not specify here will be marked as deprecated. If you do not specify this list at all, all accounts in the bank connection will be marked as deprecated. |  [optional]



