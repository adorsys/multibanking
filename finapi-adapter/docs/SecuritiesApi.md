# SecuritiesApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAndSearchAllSecurities**](SecuritiesApi.md#getAndSearchAllSecurities) | **GET** /api/v1/securities | Get and search all securities
[**getMultipleSecurities**](SecuritiesApi.md#getMultipleSecurities) | **GET** /api/v1/securities/{ids} | Get multiple securities
[**getSecurity**](SecuritiesApi.md#getSecurity) | **GET** /api/v1/securities/{id} | Get a security


<a name="getAndSearchAllSecurities"></a>
# **getAndSearchAllSecurities**
> PageableSecurityList getAndSearchAllSecurities(ids, search, accountIds, page, perPage, order)

Get and search all securities

Get securities of the user that is authorized by the access_token. Must pass the user&#39;s access_token. You can set optional search criteria to get only those securities that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.&lt;p&gt;Note: Whenever a security account is being updated, its security positions will be internally re-created, meaning that the identifier of a security position might change over time.&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.SecuritiesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

SecuritiesApi apiInstance = new SecuritiesApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of security identifiers. If specified, then only securities whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String search = "search_example"; // String | If specified, then only those securities will be contained in the result whose 'name', 'isin' or 'wkn' contains the given search string (the matching works case-insensitive). If no securities contain the search string in any of these fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for a security to get included into the result.
List<Long> accountIds = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of accounts
Integer page = 1; // Integer | Result page that you want to retrieve.
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by next fields: 'id', 'name', 'isin', 'wkn', 'quote', 'quantityNominal', 'marketValue' and 'entryQuote'. The default order for all services is 'id,asc'. You can also order by multiple properties. In that case the order of the parameters passed is important. The general format is: 'property[,asc|desc]', with 'asc' being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC). 
try {
    PageableSecurityList result = apiInstance.getAndSearchAllSecurities(ids, search, accountIds, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SecuritiesApi#getAndSearchAllSecurities");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of security identifiers. If specified, then only securities whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **search** | **String**| If specified, then only those securities will be contained in the result whose &#39;name&#39;, &#39;isin&#39; or &#39;wkn&#39; contains the given search string (the matching works case-insensitive). If no securities contain the search string in any of these fields, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the searched fields in order for a security to get included into the result. | [optional]
 **accountIds** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of accounts | [optional]
 **page** | **Integer**| Result page that you want to retrieve. | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by next fields: &#39;id&#39;, &#39;name&#39;, &#39;isin&#39;, &#39;wkn&#39;, &#39;quote&#39;, &#39;quantityNominal&#39;, &#39;marketValue&#39; and &#39;entryQuote&#39;. The default order for all services is &#39;id,asc&#39;. You can also order by multiple properties. In that case the order of the parameters passed is important. The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC).  | [optional]

### Return type

[**PageableSecurityList**](PageableSecurityList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleSecurities"></a>
# **getMultipleSecurities**
> SecurityList getMultipleSecurities(ids)

Get multiple securities

Get a list of multiple securities of the user that is authorized by the access_token. Must pass the securities&#39; identifiers and the user&#39;s access_token. Securities whose identifiers do not exist or do not relate to the authorized user will not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list). &lt;p&gt;Note: Whenever a security account is being updated, its security positions will be internally re-created, meaning that the identifier of a security position might change over time.&lt;/p&gt;&lt;p&gt;WARNING: This service is deprecated and will be removed at some point. If you want to get multiple securities, please instead use the service &#39;Get and search all securities&#39; and pass a comma-separated list of identifiers as a parameter &#39;ids&#39;.&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.SecuritiesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

SecuritiesApi apiInstance = new SecuritiesApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested securities
try {
    SecurityList result = apiInstance.getMultipleSecurities(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SecuritiesApi#getMultipleSecurities");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested securities |

### Return type

[**SecurityList**](SecurityList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getSecurity"></a>
# **getSecurity**
> Security getSecurity(id)

Get a security

Get a single security for a specific user. Must pass the security&#39;s identifier and the user&#39;s access_token. &lt;p&gt;Note: Whenever a security account is being updated, its security positions will be internally re-created, meaning that the identifier of a security position might change over time.&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.SecuritiesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

SecuritiesApi apiInstance = new SecuritiesApi();
Long id = 789L; // Long | Security identifier
try {
    Security result = apiInstance.getSecurity(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SecuritiesApi#getSecurity");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Security identifier |

### Return type

[**Security**](Security.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

