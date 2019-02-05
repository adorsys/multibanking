# BankConnectionsApi

All URIs are relative to *https://localhost*

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
> IdentifierList deleteAllBankConnections()

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
    IdentifierList result = apiInstance.deleteAllBankConnections();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#deleteAllBankConnections");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**IdentifierList**](IdentifierList.md)

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
> BankConnection editBankConnection(id, body)

Edit a bank connection

Change the stored authentication credentials (banking user ID, banking customer ID, and banking PIN), or other fields of the bank connection. Must pass the connection&#39;s identifier and the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Note that a bank connection&#39;s credentials cannot be changed while it is in the process of import, update, or transactions categorization.&lt;br/&gt;&lt;br/&gt;NOTE: Depending on your license, this service may respond with HTTP code 451, containing an error message with a identifier of web form in it. In addition to that the response will also have included a &#39;Location&#39; header, which contains the URL to the web form. In this case, you must forward your user to finAPI&#39;s web form. For a detailed explanation of the Web Form Flow, please refer to this article: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/360002596391&#39; target&#x3D;&#39;_blank&#39;&gt;finAPI&#39;s Web Form Flow&lt;/a&gt;

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
EditBankConnectionParams body = new EditBankConnectionParams(); // EditBankConnectionParams | New bank connection parameters
try {
    BankConnection result = apiInstance.editBankConnection(id, body);
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
 **body** | [**EditBankConnectionParams**](EditBankConnectionParams.md)| New bank connection parameters |

### Return type

[**BankConnection**](BankConnection.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAllBankConnections"></a>
# **getAllBankConnections**
> BankConnectionList getAllBankConnections(ids)

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
    BankConnectionList result = apiInstance.getAllBankConnections(ids);
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

[**BankConnectionList**](BankConnectionList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getBankConnection"></a>
# **getBankConnection**
> BankConnection getBankConnection(id)

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
    BankConnection result = apiInstance.getBankConnection(id);
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

[**BankConnection**](BankConnection.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleBankConnections"></a>
# **getMultipleBankConnections**
> BankConnectionList getMultipleBankConnections(ids)

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
    BankConnectionList result = apiInstance.getMultipleBankConnections(ids);
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

[**BankConnectionList**](BankConnectionList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="importBankConnection"></a>
# **importBankConnection**
> BankConnection importBankConnection(body)

Import a new bank connection

Imports a new bank connection for a specific user. Must pass the connection credentials and the user&#39;s access_token. All bank accounts will be downloaded and imported with their current balances, transactions and supported two-step-procedures (note that the amount of available transactions may vary between banks, e.g. some banks deliver all transactions from the past year, others only deliver the transactions from the past three months). The balance and transactions download process runs asynchronously, so this service may return before all balances and transactions have been imported. Also, all downloaded transactions will be categorized by a separate background process that runs asynchronously too. To check the status of the balance and transactions download process as well as the background categorization process, see the status flags that are returned by the GET /bankConnections/&lt;id&gt; service.&lt;br/&gt;&lt;br/&gt;Note that some banks may require a multi-step authentication, in which case the service will respond with HTTP code 510 and an error message containing a challenge for the user from the bank. You must display the challenge message to the user, and then retry the service call, passing the user&#39;s answer to the bank&#39;s challenge in the &#39;challengeResponse&#39; field.&lt;br/&gt;&lt;br/&gt;You can also import a \&quot;demo connection\&quot; which contains a single bank account with some pre-defined transactions. To import the demo connection, you need to pass the identifier of the \&quot;finAPI Test Bank\&quot;. In case of demo connection import, any other fields besides the bank identifier can remain unset. The bankingUserId, bankingCustomerId, bankingPin, and storePin fields will be stored if you pass them, however they will not be regarded when updating the demo connection (in other words: It doesn&#39;t matter what credentials you choose for the demo connection). Note however that if you want to import the demo connection multiple times for the same user, you must use a different bankingUserId and/or bankingCustomerId for each of the imports. Also note that the skipPositionsDownload flag is ignored for the demo bank connection, i.e. when importing the demo bank connection, you will always get the transactions for its account. You can enable multi-step authentication for the demo bank connection by setting the bank connection name to \&quot;MSA\&quot;.&lt;br/&gt;&lt;br/&gt;&lt;b&gt;For a more in-depth understanding of the import process, please also read this article on our Dev Portal: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115000296607-Import-Update-of-Bank-Connections-Accounts&#39; target&#x3D;&#39;_blank&#39;&gt;Import &amp; Update of Bank Connections / Accounts&lt;/a&gt;&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;NOTE: Depending on your license, this service may respond with HTTP code 451, containing an error message with a identifier of web form in it. In addition to that the response will also have included a &#39;Location&#39; header, which contains the URL to the web form. In this case, you must forward your user to finAPI&#39;s web form. For a detailed explanation of the Web Form Flow, please refer to this article: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/360002596391&#39; target&#x3D;&#39;_blank&#39;&gt;finAPI&#39;s Web Form Flow&lt;/a&gt;

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
ImportBankConnectionParams body = new ImportBankConnectionParams(); // ImportBankConnectionParams | Import bank connection parameters
try {
    BankConnection result = apiInstance.importBankConnection(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#importBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ImportBankConnectionParams**](ImportBankConnectionParams.md)| Import bank connection parameters |

### Return type

[**BankConnection**](BankConnection.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="updateBankConnection"></a>
# **updateBankConnection**
> BankConnection updateBankConnection(body)

Update a bank connection

Update an existing bank connection of the user that is authorized by the access_token. Downloads and imports the current account balances and new transactions. Must pass the connection&#39;s identifier and the user&#39;s access_token. For more information about the processes of authentication, data download and transactions categorization, see POST /bankConnections/import. Note that supported two-step-procedures are updated as well. It may unset the current default two-step-procedure of the given bank connection (but only if this procedure is not supported anymore by the bank). You can also update the \&quot;demo connection\&quot; (in this case, the fields &#39;bankingPin&#39;, &#39;importNewAccounts&#39;, and &#39;skipPositionsDownload&#39; will be ignored).&lt;br/&gt;&lt;br/&gt;Note that you cannot trigger an update of a bank connection as long as there is still a previously triggered update running.&lt;br/&gt;&lt;br/&gt;&lt;b&gt;For a more in-depth understanding of the update process, please also read this article on our Dev Portal: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115000296607-Import-Update-of-Bank-Connections-Accounts&#39; target&#x3D;&#39;_blank&#39;&gt;Import &amp; Update of Bank Connections / Accounts&lt;/a&gt;&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;NOTE: Depending on your license, this service may respond with HTTP code 451, containing an error message with a identifier of web form in it. In addition to that the response will also have included a &#39;Location&#39; header, which contains the URL to the web form. In this case, you must forward your user to finAPI&#39;s web form. For a detailed explanation of the Web Form Flow, please refer to this article: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/360002596391&#39; target&#x3D;&#39;_blank&#39;&gt;finAPI&#39;s Web Form Flow&lt;/a&gt;

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
UpdateBankConnectionParams body = new UpdateBankConnectionParams(); // UpdateBankConnectionParams | Update bank connection parameters
try {
    BankConnection result = apiInstance.updateBankConnection(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BankConnectionsApi#updateBankConnection");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**UpdateBankConnectionParams**](UpdateBankConnectionParams.md)| Update bank connection parameters |

### Return type

[**BankConnection**](BankConnection.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

