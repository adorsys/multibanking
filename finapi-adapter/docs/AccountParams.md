
# AccountParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**accountName** | **String** | Account name. Maximum length is 512. |  [optional]
**accountTypeId** | **Long** | Identifier of account type.&lt;br/&gt;&lt;br/&gt;1 &#x3D; Checking,&lt;br/&gt;2 &#x3D; Savings,&lt;br/&gt;3 &#x3D; CreditCard,&lt;br/&gt;4 &#x3D; Security,&lt;br/&gt;5 &#x3D; Loan,&lt;br/&gt;6 &#x3D; Pocket (DEPRECATED; will not be returned for any account unless this type has explicitly been set via PATCH),&lt;br/&gt;7 &#x3D; Membership,&lt;br/&gt;8 &#x3D; Bausparen&lt;br/&gt;&lt;br/&gt; |  [optional]
**isNew** | **Boolean** | Whether this account should be flagged as &#39;new&#39; or not. Any newly imported account will have this flag initially set to true, and remain so until you set it to false (see PATCH /accounts/&lt;id&gt;). How you use this field is up to your interpretation, however it is recommended to set the flag to false for all accounts right after the initial import of the bank connection. This way, you will be able recognize accounts that get newly imported during a later update of the bank connection, by checking for any accounts with the flag set to true after every update of the bank connection. |  [optional]



