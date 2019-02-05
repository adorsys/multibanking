# TransactionsApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteAllTransactions**](TransactionsApi.md#deleteAllTransactions) | **DELETE** /api/v1/transactions | Delete all transactions
[**deleteTransaction**](TransactionsApi.md#deleteTransaction) | **DELETE** /api/v1/transactions/{id} | Delete a transaction
[**editMultipleTransactions**](TransactionsApi.md#editMultipleTransactions) | **PATCH** /api/v1/transactions | Edit multiple transactions
[**editMultipleTransactionsDeprecated**](TransactionsApi.md#editMultipleTransactionsDeprecated) | **PATCH** /api/v1/transactions/{ids} | Edit multiple transactions (DEPRECATED)
[**editTransaction**](TransactionsApi.md#editTransaction) | **PATCH** /api/v1/transactions/{id} | Edit a transaction
[**getAndSearchAllTransactions**](TransactionsApi.md#getAndSearchAllTransactions) | **GET** /api/v1/transactions | Get and search all transactions
[**getMultipleTransactions**](TransactionsApi.md#getMultipleTransactions) | **GET** /api/v1/transactions/{ids} | Get multiple transactions
[**getTransaction**](TransactionsApi.md#getTransaction) | **GET** /api/v1/transactions/{id} | Get a transaction
[**restoreTransaction**](TransactionsApi.md#restoreTransaction) | **POST** /api/v1/transactions/{id}/restore | Restore a transaction
[**splitTransaction**](TransactionsApi.md#splitTransaction) | **POST** /api/v1/transactions/{id}/split | Split a transaction
[**triggerCategorization**](TransactionsApi.md#triggerCategorization) | **POST** /api/v1/transactions/triggerCategorization | Trigger categorization


<a name="deleteAllTransactions"></a>
# **deleteAllTransactions**
> IdentifierList deleteAllTransactions(maxDeletionDate, safeMode, rememberDeletion)

Delete all transactions

Delete a set, or the entirety, of transactions of the currently authorized user.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
String maxDeletionDate = "maxDeletionDate_example"; // String | If specified, then only those transactions are being deleted whose 'finapiBookingDate' is equal to or earlier to the given date. The date may not be in future, and must be given in the format 'YYYY-MM-DD'. If not specified, then no date limitation will be in place for the deletion.
Boolean safeMode = true; // Boolean | When passing 'true', then only those transactions are being deleted where at least one of the following holds true: 1. The transaction belongs to a 'demo connection'; 2. The transaction's 'potentialDuplicate' flag is set to TRUE; 3. The transaction is an adjusting entry ('Zwischensaldo' transaction) that was added by finAPI. When passing 'false', then finAPI will delete transactions independent of these characteristics. The default value for this parameter is 'true'.
Boolean rememberDeletion = false; // Boolean | When passing 'true', then finAPI will make sure to not re-import deleted transactions on future account updates. When 'false', then deleted transactions might be re-imported. Default value for this parameter is 'false'.
try {
    IdentifierList result = apiInstance.deleteAllTransactions(maxDeletionDate, safeMode, rememberDeletion);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#deleteAllTransactions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **maxDeletionDate** | **String**| If specified, then only those transactions are being deleted whose &#39;finapiBookingDate&#39; is equal to or earlier to the given date. The date may not be in future, and must be given in the format &#39;YYYY-MM-DD&#39;. If not specified, then no date limitation will be in place for the deletion. | [optional]
 **safeMode** | **Boolean**| When passing &#39;true&#39;, then only those transactions are being deleted where at least one of the following holds true: 1. The transaction belongs to a &#39;demo connection&#39;; 2. The transaction&#39;s &#39;potentialDuplicate&#39; flag is set to TRUE; 3. The transaction is an adjusting entry (&#39;Zwischensaldo&#39; transaction) that was added by finAPI. When passing &#39;false&#39;, then finAPI will delete transactions independent of these characteristics. The default value for this parameter is &#39;true&#39;. | [optional] [default to true]
 **rememberDeletion** | **Boolean**| When passing &#39;true&#39;, then finAPI will make sure to not re-import deleted transactions on future account updates. When &#39;false&#39;, then deleted transactions might be re-imported. Default value for this parameter is &#39;false&#39;. | [optional] [default to false]

### Return type

[**IdentifierList**](IdentifierList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteTransaction"></a>
# **deleteTransaction**
> deleteTransaction(id)

Delete a transaction

Delete a single transaction of the user that is authorized by the access_token.&lt;br/&gt;&lt;br/&gt; A transaction can only get deleted if at least one of the following holds true:&lt;br/&gt; &amp;bull; The transaction belongs to a &#39;demo connection&#39;&lt;br/&gt; &amp;bull; The transaction&#39;s &#39;potentialDuplicate&#39; flag is set to TRUE&lt;br/&gt; &amp;bull; The transaction is an adjusting entry (&#39;Zwischensaldo&#39; transaction) that was added by finAPI&lt;br/&gt;&lt;br/&gt;Note that the &#39;Delete all transactions&#39; service has additional functionality and allows you to delete transactions that you cannot delete with this service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
Long id = 789L; // Long | Identifier of transaction
try {
    apiInstance.deleteTransaction(id);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#deleteTransaction");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of transaction |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editMultipleTransactions"></a>
# **editMultipleTransactions**
> IdentifierList editMultipleTransactions(body)

Edit multiple transactions

Edit one or multiple transactions. You can edit the following fields: &#39;isNew&#x3D;true|false&#39; and/or &#39;isPotentialDuplicate&#x3D;false&#39; and/or &#39;categoryId&#x3D;&lt;id&gt;&#39; and/or &#39;labelIds&#x3D;[&lt;ids&gt;]&#39;. To clear the category of the given transactions (so that they are no longer categorized), pass the value &#39;0&#39; as the categoryId. To clear the labels of the given transactions, pass an empty array of label identifiers: &#39;[]&#39;. The parameters &#39;categoryId&#39; and &#39;labelIds&#39; are forbidden if &#39;ids&#39; is NOT set (i.e. you cannot update the category or labels for ALL transactions). The result is a list of identifiers of only those transactions that have changed as a result of this service call.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
UpdateMultipleTransactionsParams body = new UpdateMultipleTransactionsParams(); // UpdateMultipleTransactionsParams | Update transactions parameters
try {
    IdentifierList result = apiInstance.editMultipleTransactions(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#editMultipleTransactions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**UpdateMultipleTransactionsParams**](UpdateMultipleTransactionsParams.md)| Update transactions parameters |

### Return type

[**IdentifierList**](IdentifierList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editMultipleTransactionsDeprecated"></a>
# **editMultipleTransactionsDeprecated**
> IdentifierList editMultipleTransactionsDeprecated(ids, body)

Edit multiple transactions (DEPRECATED)

Change the fields of multiple transactions. You can change the following fields: &#39;isNew&#x3D;true|false&#39; and/or &#39;isPotentialDuplicate&#x3D;false&#39; and/or &#39;categoryId&#x3D;&lt;id&gt;&#39; and/or &#39;labelIds&#x3D;[&lt;ids&gt;]&#39;. The max number of input identifiers is limited to 100. To clear the category of the given transactions (so that they are no longer categorized), pass the value &#39;0&#39; as the categoryId. To clear the labels of the given transactions, pass an empty array of label identifiers: &#39;[]&#39;. The result is a list of identifiers of only those transactions that have changed as a result of this service call. WARNING: This service is deprecated and will be removed at some point. If you want to edit multiple transactions, please instead use the service &#39;Edit multiple transactions&#39;, where you can pass a comma-separated list of identifiers.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of updated transactions
UpdateTransactionsParams body = new UpdateTransactionsParams(); // UpdateTransactionsParams | Update transactions parameters
try {
    IdentifierList result = apiInstance.editMultipleTransactionsDeprecated(ids, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#editMultipleTransactionsDeprecated");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of updated transactions |
 **body** | [**UpdateTransactionsParams**](UpdateTransactionsParams.md)| Update transactions parameters |

### Return type

[**IdentifierList**](IdentifierList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editTransaction"></a>
# **editTransaction**
> Transaction editTransaction(id, body)

Edit a transaction

Change a transaction&#39;s fields. You can change the following fields: &#39;isNew&#x3D;true|false&#39; and/or &#39;isPotentialDuplicate&#x3D;false&#39; and/or &#39;categoryId&#x3D;&lt;id&gt;&#39; and/or &#39;labelIds&#x3D;[&lt;ids&gt;]&#39;. To clear a transaction&#39;s category (so that it is no longer categorized), pass the value &#39;0&#39; as the categoryId. To clear the labels of the given transaction, pass an empty array of label identifiers: &#39;[]&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
Long id = 789L; // Long | Identifier of transaction
UpdateTransactionsParams body = new UpdateTransactionsParams(); // UpdateTransactionsParams | Update transactions parameters
try {
    Transaction result = apiInstance.editTransaction(id, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#editTransaction");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of transaction |
 **body** | [**UpdateTransactionsParams**](UpdateTransactionsParams.md)| Update transactions parameters |

### Return type

[**Transaction**](Transaction.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAndSearchAllTransactions"></a>
# **getAndSearchAllTransactions**
> PageableTransactionList getAndSearchAllTransactions(view, ids, search, counterpart, purpose, accountIds, minBankBookingDate, maxBankBookingDate, minFinapiBookingDate, maxFinapiBookingDate, minAmount, maxAmount, direction, labelIds, categoryIds, includeChildCategories, isNew, isPotentialDuplicate, isAdjustingEntry, minImportDate, maxImportDate, page, perPage, order)

Get and search all transactions

Get transactions of the user that is authorized by the access_token. Must pass the user&#39;s access_token. You can set optional search criteria to get only those transactions that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
String view = "view_example"; // String | This parameter defines finAPI's logical view on the transactions when querying them: 'bankView' regards only the original transactions as they were received from the bank, without considering how the transactions might have gotten split by the user (see POST /transactions/<id>/split). This means that if a transaction is split into logical sub-transactions, then the service will still regard only the original transaction, and NOT the logical sub-transactions in its processing (though for convenience, the transactions will have the data of their sub-transactions included in the response). 'userView' by contrast regards the transactions as they exist for the user. For transactions that have not been split into logical sub-transactions, there is no difference to the \"bankView\". But for transaction that have been split into logical sub-transactions, the service will ONLY regard these sub-transactions, and not the originally received transaction (though for convenience, the sub-transactions will have the identifier of their original transaction included in the response).
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of transaction identifiers. If specified, then only transactions whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String search = "search_example"; // String | If specified, then only those transactions will be contained in the result whose 'purpose' or counterpart fields contain the given search string (the matching works case-insensitive). If no transactions contain the search string in any of these fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for a transaction to get included into the result.
String counterpart = "counterpart_example"; // String | If specified, then only those transactions will be contained in the result whose counterpart fields contain the given search string (the matching works case-insensitive). If no transactions contain the search string in any of the counterpart fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for a transaction to get included into the result.
String purpose = "purpose_example"; // String | If specified, then only those transactions will be contained in the result whose purpose field contains the given search string (the matching works case-insensitive). If no transactions contain the search string in the purpose field, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the purpose in order for a transaction to get included into the result.
List<Long> accountIds = Arrays.asList(56L); // List<Long> | A comma-separated list of account identifiers. If specified, then only transactions that relate to the given accounts will be regarded. If not specified, then all accounts will be regarded.
String minBankBookingDate = "minBankBookingDate_example"; // String | Lower bound for a transaction's booking date as returned by the bank (= original booking date), in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'bankBookingDate' is equal to or later than the given date will be regarded.
String maxBankBookingDate = "maxBankBookingDate_example"; // String | Upper bound for a transaction's booking date as returned by the bank (= original booking date), in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'bankBookingDate' is equal to or earlier than the given date will be regarded.
String minFinapiBookingDate = "minFinapiBookingDate_example"; // String | Lower bound for a transaction's booking date as set by finAPI, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). For details about the meaning of the finAPI booking date, please see the field's documentation in the service's response.
String maxFinapiBookingDate = "maxFinapiBookingDate_example"; // String | Upper bound for a transaction's booking date as set by finAPI, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). For details about the meaning of the finAPI booking date, please see the field's documentation in the service's response.
BigDecimal minAmount = new BigDecimal(); // BigDecimal | If specified, then only transactions whose amount is equal to or greater than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95
BigDecimal maxAmount = new BigDecimal(); // BigDecimal | If specified, then only transactions whose amount is equal to or less than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95
String direction = "all"; // String | If specified, then only transactions with the given direction(s) will be regarded. Use 'income' for regarding only received payments (amount >= 0), 'spending' for regarding only outgoing payments (amount < 0), or 'all' to regard both directions. If not specified, the direction defaults to 'all'.
List<Long> labelIds = Arrays.asList(56L); // List<Long> | A comma-separated list of label identifiers. If specified, then only transactions that have been marked with at least one of the given labels will be contained in the result.
List<Long> categoryIds = Arrays.asList(56L); // List<Long> | A comma-separated list of category identifiers. If specified, then the result will contain only transactions whose category is either one of the given categories, or - but only if the 'includeChildCategories' flag is set to 'true' - whose category is a sub-category of one of the given categories. To include transactions without any category, pass the value '0' as the categoryId.
Boolean includeChildCategories = true; // Boolean | This flag controls how the given 'categoryIds' are handled. If set to 'true', then all transactions of a given categoryId, as well as all transactions of any of its sub-categories will be regarded. If set to 'false', then sub-categories of a given categoryId will not be regarded and only those transactions are regarded whose category matches one of the explicitly given categoryIds. The default value for this flag is 'true'.
Boolean isNew = true; // Boolean | If specified, then only transactions that have their 'isNew' flag set to true/false will be regarded.
Boolean isPotentialDuplicate = true; // Boolean | If specified, then only transactions that have their 'isPotentialDuplicate' flag set to true/false will be regarded.
Boolean isAdjustingEntry = true; // Boolean | If specified, then only transactions that have their 'isAdjustingEntry' flag set to true/false will be regarded.
String minImportDate = "minImportDate_example"; // String | Lower bound for a transaction's import date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'importDate' is equal to or later than the given date will be regarded.
String maxImportDate = "maxImportDate_example"; // String | Upper bound for a transaction's import date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'importDate' is equal to or earlier than the given date will be regarded.
Integer page = 1; // Integer | Result page that you want to retrieve.
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can use the following fields for ordering the response: 'id', 'parentId', 'accountId', 'valueDate', 'bankBookingDate', 'finapiBookingDate', 'amount', 'purpose', 'counterpartName', 'counterpartAccountNumber', 'counterpartIban', 'counterpartBlz', 'counterpartBic', 'type', 'primanota', 'category.id', 'category.name', 'isPotentialDuplicate', 'isNew' and 'importDate'. The default order for all services is 'id,asc'. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: '/transactions?order=finapiBookingDate,desc&order=counterpartName' will return the latest transactions first. If there are more transactions on the same day, then these transactions are ordered by the counterpart name (ascending). The general format is: 'property[,asc|desc]', with 'asc' being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC). 
try {
    PageableTransactionList result = apiInstance.getAndSearchAllTransactions(view, ids, search, counterpart, purpose, accountIds, minBankBookingDate, maxBankBookingDate, minFinapiBookingDate, maxFinapiBookingDate, minAmount, maxAmount, direction, labelIds, categoryIds, includeChildCategories, isNew, isPotentialDuplicate, isAdjustingEntry, minImportDate, maxImportDate, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#getAndSearchAllTransactions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **view** | **String**| This parameter defines finAPI&#39;s logical view on the transactions when querying them: &#39;bankView&#39; regards only the original transactions as they were received from the bank, without considering how the transactions might have gotten split by the user (see POST /transactions/&lt;id&gt;/split). This means that if a transaction is split into logical sub-transactions, then the service will still regard only the original transaction, and NOT the logical sub-transactions in its processing (though for convenience, the transactions will have the data of their sub-transactions included in the response). &#39;userView&#39; by contrast regards the transactions as they exist for the user. For transactions that have not been split into logical sub-transactions, there is no difference to the \&quot;bankView\&quot;. But for transaction that have been split into logical sub-transactions, the service will ONLY regard these sub-transactions, and not the originally received transaction (though for convenience, the sub-transactions will have the identifier of their original transaction included in the response). | [enum: bankView, userView]
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of transaction identifiers. If specified, then only transactions whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **search** | **String**| If specified, then only those transactions will be contained in the result whose &#39;purpose&#39; or counterpart fields contain the given search string (the matching works case-insensitive). If no transactions contain the search string in any of these fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for a transaction to get included into the result. | [optional]
 **counterpart** | **String**| If specified, then only those transactions will be contained in the result whose counterpart fields contain the given search string (the matching works case-insensitive). If no transactions contain the search string in any of the counterpart fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for a transaction to get included into the result. | [optional]
 **purpose** | **String**| If specified, then only those transactions will be contained in the result whose purpose field contains the given search string (the matching works case-insensitive). If no transactions contain the search string in the purpose field, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the purpose in order for a transaction to get included into the result. | [optional]
 **accountIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of account identifiers. If specified, then only transactions that relate to the given accounts will be regarded. If not specified, then all accounts will be regarded. | [optional]
 **minBankBookingDate** | **String**| Lower bound for a transaction&#39;s booking date as returned by the bank (&#x3D; original booking date), in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;bankBookingDate&#39; is equal to or later than the given date will be regarded. | [optional]
 **maxBankBookingDate** | **String**| Upper bound for a transaction&#39;s booking date as returned by the bank (&#x3D; original booking date), in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;bankBookingDate&#39; is equal to or earlier than the given date will be regarded. | [optional]
 **minFinapiBookingDate** | **String**| Lower bound for a transaction&#39;s booking date as set by finAPI, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). For details about the meaning of the finAPI booking date, please see the field&#39;s documentation in the service&#39;s response. | [optional]
 **maxFinapiBookingDate** | **String**| Upper bound for a transaction&#39;s booking date as set by finAPI, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). For details about the meaning of the finAPI booking date, please see the field&#39;s documentation in the service&#39;s response. | [optional]
 **minAmount** | **BigDecimal**| If specified, then only transactions whose amount is equal to or greater than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95 | [optional]
 **maxAmount** | **BigDecimal**| If specified, then only transactions whose amount is equal to or less than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95 | [optional]
 **direction** | **String**| If specified, then only transactions with the given direction(s) will be regarded. Use &#39;income&#39; for regarding only received payments (amount &gt;&#x3D; 0), &#39;spending&#39; for regarding only outgoing payments (amount &lt; 0), or &#39;all&#39; to regard both directions. If not specified, the direction defaults to &#39;all&#39;. | [optional] [default to all] [enum: all, income, spending]
 **labelIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of label identifiers. If specified, then only transactions that have been marked with at least one of the given labels will be contained in the result. | [optional]
 **categoryIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of category identifiers. If specified, then the result will contain only transactions whose category is either one of the given categories, or - but only if the &#39;includeChildCategories&#39; flag is set to &#39;true&#39; - whose category is a sub-category of one of the given categories. To include transactions without any category, pass the value &#39;0&#39; as the categoryId. | [optional]
 **includeChildCategories** | **Boolean**| This flag controls how the given &#39;categoryIds&#39; are handled. If set to &#39;true&#39;, then all transactions of a given categoryId, as well as all transactions of any of its sub-categories will be regarded. If set to &#39;false&#39;, then sub-categories of a given categoryId will not be regarded and only those transactions are regarded whose category matches one of the explicitly given categoryIds. The default value for this flag is &#39;true&#39;. | [optional] [default to true]
 **isNew** | **Boolean**| If specified, then only transactions that have their &#39;isNew&#39; flag set to true/false will be regarded. | [optional]
 **isPotentialDuplicate** | **Boolean**| If specified, then only transactions that have their &#39;isPotentialDuplicate&#39; flag set to true/false will be regarded. | [optional]
 **isAdjustingEntry** | **Boolean**| If specified, then only transactions that have their &#39;isAdjustingEntry&#39; flag set to true/false will be regarded. | [optional]
 **minImportDate** | **String**| Lower bound for a transaction&#39;s import date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;importDate&#39; is equal to or later than the given date will be regarded. | [optional]
 **maxImportDate** | **String**| Upper bound for a transaction&#39;s import date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;importDate&#39; is equal to or earlier than the given date will be regarded. | [optional]
 **page** | **Integer**| Result page that you want to retrieve. | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can use the following fields for ordering the response: &#39;id&#39;, &#39;parentId&#39;, &#39;accountId&#39;, &#39;valueDate&#39;, &#39;bankBookingDate&#39;, &#39;finapiBookingDate&#39;, &#39;amount&#39;, &#39;purpose&#39;, &#39;counterpartName&#39;, &#39;counterpartAccountNumber&#39;, &#39;counterpartIban&#39;, &#39;counterpartBlz&#39;, &#39;counterpartBic&#39;, &#39;type&#39;, &#39;primanota&#39;, &#39;category.id&#39;, &#39;category.name&#39;, &#39;isPotentialDuplicate&#39;, &#39;isNew&#39; and &#39;importDate&#39;. The default order for all services is &#39;id,asc&#39;. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: &#39;/transactions?order&#x3D;finapiBookingDate,desc&amp;order&#x3D;counterpartName&#39; will return the latest transactions first. If there are more transactions on the same day, then these transactions are ordered by the counterpart name (ascending). The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC).  | [optional]

### Return type

[**PageableTransactionList**](PageableTransactionList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleTransactions"></a>
# **getMultipleTransactions**
> TransactionList getMultipleTransactions(ids)

Get multiple transactions

Get a list of multiple transactions of the user that is authorized by the access_token. Must pass the transactions&#39; identifiers and the user&#39;s access_token. Transactions whose identifiers do not exist or do not relate to the authorized user will not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list). WARNING: This service is deprecated and will be removed at some point. If you want to get multiple transactions, please instead use the service &#39;Get and search all transactions&#39;, where you can pass a comma-separated list of identifiers.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested transactions
try {
    TransactionList result = apiInstance.getMultipleTransactions(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#getMultipleTransactions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested transactions |

### Return type

[**TransactionList**](TransactionList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getTransaction"></a>
# **getTransaction**
> Transaction getTransaction(id)

Get a transaction

Get a single transaction of the user that is authorized by the access_token. Must pass the transaction&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
Long id = 789L; // Long | Identifier of transaction
try {
    Transaction result = apiInstance.getTransaction(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#getTransaction");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of transaction |

### Return type

[**Transaction**](Transaction.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="restoreTransaction"></a>
# **restoreTransaction**
> Transaction restoreTransaction(id)

Restore a transaction

Restore a previously split transaction. Removes all of its sub-transactions.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
Long id = 789L; // Long | Transaction identifier
try {
    Transaction result = apiInstance.restoreTransaction(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#restoreTransaction");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Transaction identifier |

### Return type

[**Transaction**](Transaction.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="splitTransaction"></a>
# **splitTransaction**
> Transaction splitTransaction(id, body)

Split a transaction

Split a transaction into several logical sub-transactions. If the given transaction is split already, all its current sub-transactions will get deleted before the new sub-transactions will get created.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
Long id = 789L; // Long | Transaction identifier
SplitTransactionsParams body = new SplitTransactionsParams(); // SplitTransactionsParams | Split transactions parameters
try {
    Transaction result = apiInstance.splitTransaction(id, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#splitTransaction");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Transaction identifier |
 **body** | [**SplitTransactionsParams**](SplitTransactionsParams.md)| Split transactions parameters |

### Return type

[**Transaction**](Transaction.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="triggerCategorization"></a>
# **triggerCategorization**
> triggerCategorization(body)

Trigger categorization

Triggers finAPI&#39;s background categorization process for all uncategorized transactions of the given bank connection(s) (or of all of the user&#39;s bank connections, if no bank connection identifiers are passed). The service returns as soon as the categorizations are scheduled. At this point, the bank connections will have their &#39;categorizationStatus&#39; set to &#39;PENDING&#39;. Use the service \&quot;Get a bank connection\&quot; or \&quot;Get all bank connections\&quot; to check when the categorization has finished (this is the case when the categorizationStatus has switched to &#39;READY&#39;).&lt;br/&gt;&lt;br/&gt;Note that if at least one of the target bank connections is currently locked at the time when you call this service (i.e. the bank connection is currently being updated, or another categorization is already scheduled for it), then no categorization will be triggered at all and the service will respond with HTTP code 422.&lt;br/&gt;&lt;br/&gt;Please also note:&lt;br/&gt;&amp;bull; finAPI&#39;s background categorization process is executed automatically whenever you import or update a bank connection (though in case of update, it will categorize only the new transactions, and not re-run categorization for previously imported transactions). This means that in general you do not have to call this service after an import or update. Use this service only when you wish to re-run the categorization of all existing uncategorized transactions.&lt;br/&gt;&amp;bull; if you wish to just manually assign categories to transactions, please use the service \&quot;Edit a transaction\&quot; or \&quot;Edit multiple transactions\&quot; instead.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.TransactionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

TransactionsApi apiInstance = new TransactionsApi();
TriggerCategorizationParams body = new TriggerCategorizationParams(); // TriggerCategorizationParams | Trigger categorization parameters
try {
    apiInstance.triggerCategorization(body);
} catch (ApiException e) {
    System.err.println("Exception when calling TransactionsApi#triggerCategorization");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TriggerCategorizationParams**](TriggerCategorizationParams.md)| Trigger categorization parameters |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

