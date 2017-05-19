# AccountsApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteAccount**](AccountsApi.md#deleteAccount) | **DELETE** /api/v1/accounts/{id} | Delete an account
[**deleteAllAccounts**](AccountsApi.md#deleteAllAccounts) | **DELETE** /api/v1/accounts | Delete all accounts
[**editAccount**](AccountsApi.md#editAccount) | **PATCH** /api/v1/accounts/{id} | Edit an account
[**executeSepaMoneyTransfer**](AccountsApi.md#executeSepaMoneyTransfer) | **POST** /api/v1/accounts/executeSepaMoneyTransfer | Execute SEPA Money Transfer
[**getAccount**](AccountsApi.md#getAccount) | **GET** /api/v1/accounts/{id} | Get an account
[**getAndSearchAllAccounts**](AccountsApi.md#getAndSearchAllAccounts) | **GET** /api/v1/accounts | Get and search all accounts
[**getDailyBalances**](AccountsApi.md#getDailyBalances) | **GET** /api/v1/accounts/dailyBalances | Get daily balances
[**getMultipleAccounts**](AccountsApi.md#getMultipleAccounts) | **GET** /api/v1/accounts/{ids} | Get multiple accounts
[**requestSepaMoneyTransfer**](AccountsApi.md#requestSepaMoneyTransfer) | **POST** /api/v1/accounts/requestSepaMoneyTransfer | Request SEPA Money Transfer


<a name="deleteAccount"></a>
# **deleteAccount**
> deleteAccount(id)

Delete an account

Delete a single bank account of the user that is authorized by the access_token, including its transactions and balance data. Must pass the account&#39;s identifier and the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Notes: &lt;br/&gt;- You cannot delete an account while the bank connection that it relates to is currently in the process of import, update, or transactions categorization. &lt;br/&gt;- When the last remaining account of a bank connection gets deleted, then the bank connection itself will get deleted as well! &lt;br/&gt;- All notification rules that are connected to the account will get adjusted so that they no longer have this account listed. Notification rules that are connected to just this account (and no other accounts) will get deleted altogether.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
Long id = 789L; // Long | Identifier of the account to delete
try {
    apiInstance.deleteAccount(id);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#deleteAccount");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of the account to delete |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteAllAccounts"></a>
# **deleteAllAccounts**
> InlineResponse2001 deleteAllAccounts()

Delete all accounts

Delete all accounts of the user that is authorized by the access_token, including all transactions and balance data. Must pass the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Notes: &lt;br/&gt;- Deleting all of the user&#39;s accounts also deletes all of his bank connections. &lt;br/&gt;- All notification rules that are connected to any specific accounts will get deleted as well. &lt;br/&gt;- If at least one of the user&#39;s bank connections in currently in the process of import, update, or transactions categorization, then this service will perform no action at all.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
try {
    InlineResponse2001 result = apiInstance.deleteAllAccounts();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#deleteAllAccounts");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**InlineResponse2001**](InlineResponse2001.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editAccount"></a>
# **editAccount**
> InlineResponse200Accounts editAccount(id, body)

Edit an account

Change the name and/or the type and/or the &#39;isNew&#39; flag of a single bank account of the user that is authorized by the access_token. Must pass the account&#39;s identifier, the account&#39;s new name and/or type and/or &#39;isNew&#39; flag, and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
Long id = 789L; // Long | Identifier of the account to edit
Body2 body = new Body2(); // Body2 | New account name and/or type and/or 'isNew' flag
try {
    InlineResponse200Accounts result = apiInstance.editAccount(id, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#editAccount");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of the account to edit |
 **body** | [**Body2**](Body2.md)| New account name and/or type and/or &#39;isNew&#39; flag | [optional]

### Return type

[**InlineResponse200Accounts**](InlineResponse200Accounts.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="executeSepaMoneyTransfer"></a>
# **executeSepaMoneyTransfer**
> InlineResponse2003 executeSepaMoneyTransfer(body)

Execute SEPA Money Transfer

Execute a SEPA money transfer order that has been previously submitted by the use of the /requestSepaMoneyTransfer service. Note that this service cannot be used for the demo account.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
Body body = new Body(); // Body | Parameters for the execution of a SEPA money transfer order
try {
    InlineResponse2003 result = apiInstance.executeSepaMoneyTransfer(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#executeSepaMoneyTransfer");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body**](Body.md)| Parameters for the execution of a SEPA money transfer order |

### Return type

[**InlineResponse2003**](InlineResponse2003.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAccount"></a>
# **getAccount**
> InlineResponse200Accounts getAccount(id)

Get an account

Get a single bank account of the user that is authorized by the access_token. Must pass the account&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
Long id = 789L; // Long | Identifier of requested account
try {
    InlineResponse200Accounts result = apiInstance.getAccount(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#getAccount");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of requested account |

### Return type

[**InlineResponse200Accounts**](InlineResponse200Accounts.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAndSearchAllAccounts"></a>
# **getAndSearchAllAccounts**
> InlineResponse200 getAndSearchAllAccounts(ids, search, accountTypeIds, bankConnectionIds, minLastSuccessfulUpdate, maxLastSuccessfulUpdate, minBalance, maxBalance)

Get and search all accounts

Get bank accounts of the user that is authorized by the access_token. Must pass the user&#39;s access_token. You can set optional search criteria to get only those bank accounts that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of account identifiers. If specified, then only accounts whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String search = "search_example"; // String | If specified, then only those accounts will be contained in the result whose 'accountName', 'iban', 'accountNumber' or 'subAccountNumber' contains the given search string (the matching works case-insensitive). If no accounts contain the search string in any of these fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for an account to get included into the result.
List<Long> accountTypeIds = Arrays.asList(56L); // List<Long> | A comma-separated list of account types. If specified, then only accounts that relate to the given types will be regarded. If not specified, then all accounts will be regarded.
List<Long> bankConnectionIds = Arrays.asList(56L); // List<Long> | A comma-separated list of bank connection identifiers. If specified, then only accounts that relate to the given bank connections will be regarded. If not specified, then all accounts will be regarded.
String minLastSuccessfulUpdate = "minLastSuccessfulUpdate_example"; // String | Lower bound for a account's last successful update date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only accounts whose 'lastSuccessfulUpdate' is equal to or later than the given date will be regarded.
String maxLastSuccessfulUpdate = "maxLastSuccessfulUpdate_example"; // String | Upper bound for a account's last successful update date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only accounts whose 'lastSuccessfulUpdate' is equal to or earlier than the given date will be regarded.
BigDecimal minBalance = new BigDecimal(); // BigDecimal | If specified, then only accounts whose balance is equal to or greater than the given balance will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95
BigDecimal maxBalance = new BigDecimal(); // BigDecimal | If specified, then only accounts whose balance is equal to or less than the given balance will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95
try {
    InlineResponse200 result = apiInstance.getAndSearchAllAccounts(ids, search, accountTypeIds, bankConnectionIds, minLastSuccessfulUpdate, maxLastSuccessfulUpdate, minBalance, maxBalance);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#getAndSearchAllAccounts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of account identifiers. If specified, then only accounts whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **search** | **String**| If specified, then only those accounts will be contained in the result whose &#39;accountName&#39;, &#39;iban&#39;, &#39;accountNumber&#39; or &#39;subAccountNumber&#39; contains the given search string (the matching works case-insensitive). If no accounts contain the search string in any of these fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for an account to get included into the result. | [optional]
 **accountTypeIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of account types. If specified, then only accounts that relate to the given types will be regarded. If not specified, then all accounts will be regarded. | [optional]
 **bankConnectionIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of bank connection identifiers. If specified, then only accounts that relate to the given bank connections will be regarded. If not specified, then all accounts will be regarded. | [optional]
 **minLastSuccessfulUpdate** | **String**| Lower bound for a account&#39;s last successful update date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only accounts whose &#39;lastSuccessfulUpdate&#39; is equal to or later than the given date will be regarded. | [optional]
 **maxLastSuccessfulUpdate** | **String**| Upper bound for a account&#39;s last successful update date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only accounts whose &#39;lastSuccessfulUpdate&#39; is equal to or earlier than the given date will be regarded. | [optional]
 **minBalance** | **BigDecimal**| If specified, then only accounts whose balance is equal to or greater than the given balance will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95 | [optional]
 **maxBalance** | **BigDecimal**| If specified, then only accounts whose balance is equal to or less than the given balance will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95 | [optional]

### Return type

[**InlineResponse200**](InlineResponse200.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getDailyBalances"></a>
# **getDailyBalances**
> InlineResponse2002 getDailyBalances(accountIds, startDate, endDate, withProjection, page, perPage, order)

Get daily balances

Returns the daily balances for the given accounts and the given period. The balances are calculated by finAPI and are based on each account&#39;s latest balance.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
List<Long> accountIds = Arrays.asList(56L); // List<Long> | A comma-separated list of (non-security) account identifiers. If no accounts are specified, all (non-security) accounts of the current user are regarded.
String startDate = "startDate_example"; // String | A string in the format 'YYYY-MM-DD'. Note that the requested date range [startDate..endDate] may not exceed 365 days. If startDate is not specified, it defaults to the endDate minus one month.
String endDate = "endDate_example"; // String | A string in the format 'YYYY-MM-DD'. Note that the requested date range [startDate..endDate] may not exceed 365 days. If endDate is not specified, it defaults to today's date.
Boolean withProjection = true; // Boolean | Whether finAPI should project the first and last actually existing balance into the past and future. When passing 'true', then the result will always contain a daily balance for every day of the entire requested date range, even for days before the first actually existing balance, resp. after the last actually existing balance. Those days will have the same balance as the day of the first actual balance, resp. last actual balance, i.e. the first/last balance will be infinitely projected into the past/the future. When passing 'false', then the result will contain daily balances only from the day on where the first actual balance exists for any of the given accounts, and only up to the day where the last actual balance exists for any of the given accounts. Note that when in this case there are no actual balances within the requested date range, then an empty array will be returned. Default value for this parameter is 'true'.
Integer page = 1; // Integer | Result page that you want to retrieve.
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by id, name or bic. The default order for this service is 'date,asc'. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: '/accounts/dailyBalances?order=date,desc&order=balance,asc' will return daily balances ordered by 'date' (descending), where items with the same 'date' are ordered by 'balance' (ascending). The general format is: 'property[,asc|desc]', with 'asc' being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC). 
try {
    InlineResponse2002 result = apiInstance.getDailyBalances(accountIds, startDate, endDate, withProjection, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#getDailyBalances");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **accountIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of (non-security) account identifiers. If no accounts are specified, all (non-security) accounts of the current user are regarded. | [optional]
 **startDate** | **String**| A string in the format &#39;YYYY-MM-DD&#39;. Note that the requested date range [startDate..endDate] may not exceed 365 days. If startDate is not specified, it defaults to the endDate minus one month. | [optional]
 **endDate** | **String**| A string in the format &#39;YYYY-MM-DD&#39;. Note that the requested date range [startDate..endDate] may not exceed 365 days. If endDate is not specified, it defaults to today&#39;s date. | [optional]
 **withProjection** | **Boolean**| Whether finAPI should project the first and last actually existing balance into the past and future. When passing &#39;true&#39;, then the result will always contain a daily balance for every day of the entire requested date range, even for days before the first actually existing balance, resp. after the last actually existing balance. Those days will have the same balance as the day of the first actual balance, resp. last actual balance, i.e. the first/last balance will be infinitely projected into the past/the future. When passing &#39;false&#39;, then the result will contain daily balances only from the day on where the first actual balance exists for any of the given accounts, and only up to the day where the last actual balance exists for any of the given accounts. Note that when in this case there are no actual balances within the requested date range, then an empty array will be returned. Default value for this parameter is &#39;true&#39;. | [optional] [default to true]
 **page** | **Integer**| Result page that you want to retrieve. | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by id, name or bic. The default order for this service is &#39;date,asc&#39;. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: &#39;/accounts/dailyBalances?order&#x3D;date,desc&amp;order&#x3D;balance,asc&#39; will return daily balances ordered by &#39;date&#39; (descending), where items with the same &#39;date&#39; are ordered by &#39;balance&#39; (ascending). The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC).  | [optional]

### Return type

[**InlineResponse2002**](InlineResponse2002.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleAccounts"></a>
# **getMultipleAccounts**
> InlineResponse200 getMultipleAccounts(ids)

Get multiple accounts

Get a list of multiple bank accounts of the user that is authorized by the access_token. Must pass the accounts&#39; identifiers and the user&#39;s access_token. Accounts whose identifiers do not exist or do not relate to the authorized user will not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list). WARNING: This service is deprecated and will be removed at some point. If you want to get multiple accounts, please instead use the service &#39;Get and search all accounts&#39; and pass a comma-separated list of identifiers as a parameter &#39;ids&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested accounts
try {
    InlineResponse200 result = apiInstance.getMultipleAccounts(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#getMultipleAccounts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested accounts |

### Return type

[**InlineResponse200**](InlineResponse200.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="requestSepaMoneyTransfer"></a>
# **requestSepaMoneyTransfer**
> InlineResponse2004 requestSepaMoneyTransfer(body)

Request SEPA Money Transfer

Submit a SEPA money transfer order for either a single or a collective money transfer. Returns an instruction from the bank server that can be displayed to the user (e.g. \&quot;Enter TAN\&quot;), typically in the language of the bank&#39;s country. The order remains valid for execution for only a couple of minutes (the exact validity period depends on the bank). For executing the order, use the /executeSepaMoneyTransfer service after calling this service. Note that when the order is not executed within the validity period, the bank might take note of that and - if happening too often - ultimately lock the user&#39;s online banking access. If there already exists a previously submitted, but not yet executed money transfer order for this account, then that order will be discarded and replaced with the new order that is being created with this service call. Note that this service cannot be used for the demo account.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AccountsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

AccountsApi apiInstance = new AccountsApi();
Body1 body = new Body1(); // Body1 | Parameters for a SEPA money transfer request
try {
    InlineResponse2004 result = apiInstance.requestSepaMoneyTransfer(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AccountsApi#requestSepaMoneyTransfer");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body1**](Body1.md)| Parameters for a SEPA money transfer request |

### Return type

[**InlineResponse2004**](InlineResponse2004.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

