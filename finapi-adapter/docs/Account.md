
# Account

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Account identifier | 
**bankConnectionId** | **Long** | Identifier of the bank connection that this account belongs to | 
**accountName** | **String** | Account name |  [optional]
**accountNumber** | **String** | (National) account number. Note that this value might change whenever the account is updated (for example, leading zeros might be added or removed). | 
**subAccountNumber** | **String** | Account&#39;s sub-account-number. Note that this field can change from &#39;null&#39; to a value - or vice versa - any time when the account is being updated. This is subject to changes within the bank&#39;s internal account management. |  [optional]
**iban** | **String** | Account&#39;s IBAN. Note that this field can change from &#39;null&#39; to a value - or vice versa - any time when the account is being updated. This is subject to changes within the bank&#39;s internal account management. |  [optional]
**accountHolderName** | **String** | Name of the account holder |  [optional]
**accountHolderId** | **String** | Bank&#39;s internal identification of the account holder. Note that if your client has no license for processing this field, it will always be &#39;XXXXX&#39; |  [optional]
**accountCurrency** | **String** | Account&#39;s currency |  [optional]
**accountTypeId** | **Long** | Identifier of the account&#39;s type. Note that, in general, the type of an account can change any time when the account is being updated. This is subject to changes within the bank&#39;s internal account management. However, if the account&#39;s type has previously been changed explicitly (via the PATCH method), then the explicitly set type will NOT be automatically changed anymore, even if the type has changed on the bank side. &lt;br/&gt;1 &#x3D; Checking,&lt;br/&gt;2 &#x3D; Savings,&lt;br/&gt;3 &#x3D; CreditCard,&lt;br/&gt;4 &#x3D; Security,&lt;br/&gt;5 &#x3D; Loan,&lt;br/&gt;6 &#x3D; Pocket (DEPRECATED; will not be returned for any account unless this type has explicitly been set via PATCH),&lt;br/&gt;7 &#x3D; Membership,&lt;br/&gt;8 &#x3D; Bausparen&lt;br/&gt; | 
**accountTypeName** | **String** | Name of the account&#39;s type | 
**balance** | [**BigDecimal**](BigDecimal.md) | Current account balance |  [optional]
**overdraft** | [**BigDecimal**](BigDecimal.md) | Current overdraft |  [optional]
**overdraftLimit** | [**BigDecimal**](BigDecimal.md) | Overdraft limit |  [optional]
**availableFunds** | [**BigDecimal**](BigDecimal.md) | Current available funds. Note that this field is only set if finAPI can make a definite statement about the current available funds. This might not always be the case, for example if there is not enough information available about the overdraft limit and current overdraft. |  [optional]
**lastSuccessfulUpdate** | **String** | Timestamp of when the account was last successfully updated (or initially imported); more precisely: time when the account data (balance and positions) has been stored into the finAPI databases. The value is returned in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). |  [optional]
**lastUpdateAttempt** | **String** | Timestamp of when the account was last tried to be updated (or initially imported); more precisely: time when the update (or initial import) was triggered. The value is returned in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). |  [optional]
**isNew** | **Boolean** | Indicating whether this account is &#39;new&#39; or not. Any newly imported account will have this flag initially set to true, and remain so until you set it to false (see PATCH /accounts/&lt;id&gt;). How you use this field is up to your interpretation, however it is recommended to set the flag to false for all accounts right after the initial import of the bank connection. This way, you will be able recognize accounts that get newly imported during a later update of the bank connection, by checking for any accounts with the flag set to true right after an update. | 
**status** | [**StatusEnum**](#StatusEnum) | The current status of the account. Possible values are:&lt;br/&gt;&amp;bull; &lt;code&gt;UPDATED&lt;/code&gt; means that the account is up to date from finAPI&#39;s point of view. This means that no current import/update is running, and the previous import/update could successfully update the account&#39;s data (e.g. transactions and securities), and the bank given balance matched the transaction&#39;s calculated sum, meaning that no adjusting entry (&#39;Zwischensaldo&#39; transaction) was inserted.&lt;br/&gt;&amp;bull; &lt;code&gt;UPDATED_FIXED&lt;/code&gt; means that the account is up to date from finAPI&#39;s point of view (no current import/update is running, and the previous import/update could successfully update the account&#39;s data), BUT there was a deviation in the bank given balance which was fixed by adding an adjusting entry (&#39;Zwischensaldo&#39; transaction).&lt;br/&gt;&amp;bull; &lt;code&gt;DOWNLOAD_IN_PROGRESS&lt;/code&gt; means that the account&#39;s data is currently being imported/updated.&lt;br/&gt;&amp;bull; &lt;code&gt;DOWNLOAD_FAILED&lt;/code&gt; means that the account data could not get successfully imported or updated. Possible reasons: finAPI could not get the account&#39;s balance, or it could not parse all transactions/securities, or some internal error has occurred. Also, it could mean that finAPI could not even get to the point of receiving the account data from the bank server, for example because of incorrect login credentials or a network problem. Note however that when we get a balance and just an empty list of transactions or securities, then this is regarded as valid and successful download. The reason for this is that for some accounts that have little activity, we may actually get no recent transactions but only a balance.&lt;br/&gt;&amp;bull; &lt;code&gt;DEPRECATED&lt;/code&gt; means that the account could no longer get matched with any account from the bank server. This can mean either that the account was terminated by the user and is no longer sent by the bank server, or that finAPI could no longer match it because the account&#39;s data (name, type, iban, account number, etc.) has been changed by the bank. | 
**supportedOrders** | [**List&lt;SupportedOrdersEnum&gt;**](#List&lt;SupportedOrdersEnum&gt;) | List of orders that this account supports. Possible values are:&lt;br/&gt;&lt;br/&gt;&amp;bull; &lt;code&gt;SEPA_MONEY_TRANSFER&lt;/code&gt; - single money transfer&lt;br/&gt;&amp;bull; &lt;code&gt;SEPA_COLLECTIVE_MONEY_TRANSFER&lt;/code&gt; - collective money transfer&lt;br/&gt;&amp;bull; &lt;code&gt;SEPA_BASIC_DIRECT_DEBIT&lt;/code&gt; - single basic direct debit&lt;br/&gt;&amp;bull; &lt;code&gt;SEPA_BASIC_COLLECTIVE_DIRECT_DEBIT&lt;/code&gt; - collective basic direct debit&lt;br/&gt;&amp;bull; &lt;code&gt;SEPA_B2B_DIRECT_DEBIT&lt;/code&gt; - single Business-To-Business direct debit&lt;br/&gt;&amp;bull; &lt;code&gt;SEPA_B2B_COLLECTIVE_DIRECT_DEBIT&lt;/code&gt; - collective Business-To-Business direct debit&lt;br/&gt;&lt;br/&gt;Note that this list may be empty if the account is not supporting any of the above orders. Also note that the list is refreshed each time the account is being updated, so available orders may get added or removed in the course of an account update.&lt;br/&gt;&lt;br/&gt; | 
**clearingAccounts** | [**List&lt;ClearingAccountData&gt;**](ClearingAccountData.md) | List of clearing accounts that relate to this account. Clearing accounts can be used for money transfers (see field &#39;clearingAccountId&#39; of the &#39;Request SEPA Money Transfer&#39; service). |  [optional]


<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
UPDATED | &quot;UPDATED&quot;
UPDATED_FIXED | &quot;UPDATED_FIXED&quot;
DOWNLOAD_IN_PROGRESS | &quot;DOWNLOAD_IN_PROGRESS&quot;
DOWNLOAD_FAILED | &quot;DOWNLOAD_FAILED&quot;
DEPRECATED | &quot;DEPRECATED&quot;


<a name="List<SupportedOrdersEnum>"></a>
## Enum: List&lt;SupportedOrdersEnum&gt;
Name | Value
---- | -----
MONEY_TRANSFER | &quot;SEPA_MONEY_TRANSFER&quot;
COLLECTIVE_MONEY_TRANSFER | &quot;SEPA_COLLECTIVE_MONEY_TRANSFER&quot;
BASIC_DIRECT_DEBIT | &quot;SEPA_BASIC_DIRECT_DEBIT&quot;
BASIC_COLLECTIVE_DIRECT_DEBIT | &quot;SEPA_BASIC_COLLECTIVE_DIRECT_DEBIT&quot;
B2B_DIRECT_DEBIT | &quot;SEPA_B2B_DIRECT_DEBIT&quot;
B2B_COLLECTIVE_DIRECT_DEBIT | &quot;SEPA_B2B_COLLECTIVE_DIRECT_DEBIT&quot;



