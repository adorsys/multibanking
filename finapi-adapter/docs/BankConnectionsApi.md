# BankConnectionsApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteAllBankConnections**](BankConnectionsApi.md#deleteAllBankConnections) | **DELETE** /api/v1/bankConnections | Delete all bank connections
[**deleteBankConnection**](BankConnectionsApi.md#deleteBankConnection) | **DELETE** /api/v1/bankConnections/{id} | Delete a bank connection
[**editBankConnection**](BankConnectionsApi.md#editBankConnection) | **PATCH** /api/v1/bankConnections/{id} | Edit a bank connection
[**getAllBankConnections**](BankConnectionsApi.md#getAllBankConnections) | **GET** /api/v1/bankConnections | Get all bank connections
[**getBankConnection**](BankConnectionsApi.md#getBankConnection) | **GET** /api/v1/bankConnections/{id} | Get a bank connection
[**getMultipleBankConnections**](BankConnectionsApi.md#getMultipleBankConnections) | **GET** /api/v1/bankConnections/{ids} | Get multiple bank connections
[**importBankConnection**](BankConnectionsApi.md#importBankConnection) | **POST** /api/v1/bankConnections/import | Import a new bank connection
[**updateBankConnection**](BankConnectionsApi.md#updateBankConnection) | **POST** /api/v1/bankConnections/update | Update a bank connection


<a name="deleteAllBankConnections"></a>
# **deleteAllBankConnections**
> InlineResponse2001 deleteAllBankConnections()

Delete all bank connections

Delete all bank connections of the user that is authorized by the access_token. Must pass the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Notes: &lt;br/&gt;- All notification rules that are connected to any specific bank connection will get deleted as well. &lt;br/&gt;- If at least one bank connection is busy (currently in the process of import, update, or transactions categorization), then this service will perform no action at all.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
try {
    InlineResponse2001 result = apiInstance.deleteAllBankConnections();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#deleteAllBankConnections");
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

<a name="deleteBankConnection"></a>
# **deleteBankConnection**
> deleteBankConnection(id)

Delete a bank connection

Delete a single bank connection of the user that is authorized by the access_token, including all of its accounts and their transactions and balance data. Must pass the connection&#39;s identifier and the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Notes: &lt;br/&gt;- All notification rules that are connected to the bank connection will get adjusted so that they no longer have this connection listed. Notification rules that are connected to just this bank connection (and no other connection) will get deleted altogether. &lt;br/&gt;- A bank connection cannot get deleted while it is in the process of import, update, or transactions categorization.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
Long id = 789L; // Long | Identifier of the bank connection to delete
try {
    apiInstance.deleteBankConnection(id);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#deleteBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of the bank connection to delete |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editBankConnection"></a>
# **editBankConnection**
> InlineResponse2005Connections editBankConnection(id, body)

Edit a bank connection

Change the stored authentication credentials (banking user ID, banking customer ID, and banking PIN), or other fields of the bank connection. Must pass the connection&#39;s identifier and the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Note that a bank connection&#39;s credentials cannot be changed while it is in the process of import, update, or transactions categorization.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
Long id = 789L; // Long | Identifier of the bank connection to change the parameters for
Body5 body = new Body5(); // Body5 | New bank connection parameters
try {
    InlineResponse2005Connections result = apiInstance.editBankConnection(id, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#editBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of the bank connection to change the parameters for |
 **body** | [**Body5**](Body5.md)| New bank connection parameters |

### Return type

[**InlineResponse2005Connections**](InlineResponse2005Connections.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAllBankConnections"></a>
# **getAllBankConnections**
> InlineResponse2005 getAllBankConnections(ids)

Get all bank connections

Get bank connections of the user that is authorized by the access_token. Must pass the user&#39;s access_token. You can set optional search criteria to get only those bank connections that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of bank connection identifiers. If specified, then only bank connections whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
try {
    InlineResponse2005 result = apiInstance.getAllBankConnections(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#getAllBankConnections");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of bank connection identifiers. If specified, then only bank connections whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]

### Return type

[**InlineResponse2005**](InlineResponse2005.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getBankConnection"></a>
# **getBankConnection**
> InlineResponse2005Connections getBankConnection(id)

Get a bank connection

Get a single bank connection of the user that is authorized by the access_token. Must pass the connection&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
Long id = 789L; // Long | Identifier of requested bank connection
try {
    InlineResponse2005Connections result = apiInstance.getBankConnection(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#getBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of requested bank connection |

### Return type

[**InlineResponse2005Connections**](InlineResponse2005Connections.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleBankConnections"></a>
# **getMultipleBankConnections**
> InlineResponse2005 getMultipleBankConnections(ids)

Get multiple bank connections

Get a list of multiple bank connections of the user that is authorized by the access_token. Must pass the connections&#39; identifiers and the user&#39;s access_token. Connections whose identifiers do not exist or do not relate to the authorized user will not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list). WARNING: This service is deprecated and will be removed at some point. If you want to get multiple bank connections, please instead use the service &#39;Get all bank connections&#39; and pass a comma-separated list of identifiers as a parameter &#39;ids&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested bank connections
try {
    InlineResponse2005 result = apiInstance.getMultipleBankConnections(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#getMultipleBankConnections");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested bank connections |

### Return type

[**InlineResponse2005**](InlineResponse2005.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="importBankConnection"></a>
# **importBankConnection**
> InlineResponse2005Connections importBankConnection(body)

Import a new bank connection

Imports a new bank connection for a specific user. Must pass the connection credentials and the user&#39;s access_token. All bank accounts will be downloaded and imported with their current balances, transactions and supported two-step procedures (note that the amount of available transactions may vary between banks, e.g. some banks deliver all transactions from the past year, others only deliver the transactions from the past three months). The balance and transactions download process runs asynchronously, so this service may return before all balances and transactions have been imported. Also, all downloaded transactions will be categorized by a separate background process that runs asynchronously too. To check the status of the balance and transactions download process as well as the background categorization process, see the status flags that are returned by the GET /bankConnections/&lt;id&gt; service.&lt;br/&gt;&lt;br/&gt;You can also import a \&quot;demo connection\&quot; which contains a single bank account with some pre-defined transactions. To import the demo connection, you need to pass the identifier of the \&quot;demo bank\&quot;, which is a bank with BLZ 00000000 (see GET /banks/search). In case of demo connection import, any other fields besides the demo bank identifier can remain unset. The bankingUserId, bankingPin, and storePin fields will be stored, however they will not be relevant when updating the bank connection. Also, the skipPositionsDownload flag is ignored for the demo bank connection, i.e. when importing the demo bank connection, you will always get the transactions for its account.&lt;br/&gt;&lt;br/&gt;For a more in-depth understanding of the import process, please also read this article on our Dev Portal: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115000296607-Import-Update-of-Bank-Connections-Accounts&#39;&gt;Import &amp; Update of Bank Connections / Accounts&lt;/a&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
Body3 body = new Body3(); // Body3 | Import bank connection parameters
try {
    InlineResponse2005Connections result = apiInstance.importBankConnection(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#importBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body3**](Body3.md)| Import bank connection parameters |

### Return type

[**InlineResponse2005Connections**](InlineResponse2005Connections.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="updateBankConnection"></a>
# **updateBankConnection**
> InlineResponse2005Connections updateBankConnection(body)

Update a bank connection

Update an existing bank connection of the user that is authorized by the access_token. Downloads and imports the current account balances and new transactions. Must pass the connection&#39;s identifier and the user&#39;s access_token. For more information about the process of data download and transactions categorization, see POST /bankConnections/import. Note that supported two-step procedures are updated as well. It may unset the current default two-step procedure of the given bank connection (but only if this procedure is not supported anymore by the bank). You can also update the \&quot;demo connection\&quot; (in this case, the fields bankingPin, importNewAccounts, and skipPositionsDownload will be ignored). Note that you cannot trigger an update of a bank connection as long as there is still a previously triggered update running.&lt;br/&gt;&lt;br/&gt;For a more in-depth understanding of the update process, please also read this article on our Dev Portal: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115000296607-Import-Update-of-Bank-Connections-Accounts&#39;&gt;Import &amp; Update of Bank Connections / Accounts&lt;/a&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BankConnectionsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BankConnectionsApi apiInstance = new BankConnectionsApi();
Body4 body = new Body4(); // Body4 | Update bank connection parameters
try {
    InlineResponse2005Connections result = apiInstance.updateBankConnection(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#updateBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body4**](Body4.md)| Update bank connection parameters |

### Return type

[**InlineResponse2005Connections**](InlineResponse2005Connections.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

