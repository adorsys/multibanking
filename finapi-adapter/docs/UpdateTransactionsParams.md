
# UpdateTransactionsParams

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**isNew** | **Boolean** | Whether this transactions should be flagged as &#39;new&#39; or not. Any newly imported transaction will have this flag initially set to true. How you use this field is up to your interpretation. For example, you might want to set it to false once a user has clicked on/seen the transaction. |  [optional]
**isPotentialDuplicate** | **Boolean** | You can set this field only to &#39;false&#39;. finAPI marks transactions as a potential duplicates  when its internal duplicate detection algorithm is signaling so. Transactions that are flagged as duplicates can be deleted by the user. To prevent the user from deleting original transactions, which might lead to incorrect balances, it is not possible to manually set this flag to &#39;true&#39;. |  [optional]
**categoryId** | **Long** | Identifier of the new category to apply to the transaction. When updating the transaction&#39;s category, the category&#39;s fields &#39;id&#39;, &#39;name&#39;, &#39;parentId&#39;, &#39;parentName&#39;, and &#39;isCustom&#39; will all get updated. To clear the category for the transaction, the categoryId field must be passed with value 0. |  [optional]
**labelIds** | **List&lt;Long&gt;** | Identifiers of labels to apply to the transaction. To clear transactions&#39; labels, pass an empty array of identifiers: &#39;[]&#39; |  [optional]



