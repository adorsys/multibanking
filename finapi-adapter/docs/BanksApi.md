# BanksApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAndSearchAllBanks**](BanksApi.md#getAndSearchAllBanks) | **GET** /api/v1/banks | Get and search all banks
[**getBank**](BanksApi.md#getBank) | **GET** /api/v1/banks/{id} | Get a bank
[**getMultipleBanks**](BanksApi.md#getMultipleBanks) | **GET** /api/v1/banks/{ids} | Get multiple banks


<a name="getAndSearchAllBanks"></a>
# **getAndSearchAllBanks**
> PageableBankList getAndSearchAllBanks(ids, search, isSupported, pinsAreVolatile, supportedDataSources, location, isTestBank, page, perPage, order)

Get and search all banks

Get and search banks from finAPI&#39;s database of banks. Must pass the authorized user&#39;s access_token, or your client&#39;s access_token. You can set optional search criteria to get only those banks that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BanksApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BanksApi apiInstance = new BanksApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of bank identifiers. If specified, then only banks whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String search = "search_example"; // String | If specified, then only those banks will be contained in the result whose 'name', 'blz', 'bic' or 'city' contains the given search string (the matching works case-insensitive). If no banks contain the search string in any of the regarded fields, then the result will be an empty list. Note that you may also pass an IBAN in this field, in which case finAPI will try to find the related bank in its database and regard only this bank for the search. Also note: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must apply to a bank in order for it to get included into the result.
Boolean isSupported = true; // Boolean | If specified, then only supported (in case of 'true' value) or unsupported (in case of 'false' value) banks will be regarded.
Boolean pinsAreVolatile = true; // Boolean | If specified, then only those banks will be regarded that have the given value (true or false) for their 'pinsAreVolatile' field.
List<String> supportedDataSources = Arrays.asList("supportedDataSources_example"); // List<String> | Comma-separated list of data sources. Possible values: WEB_SCRAPER,FINTS_SERVER. If this parameter is specified, then only those banks will be regarded in the search that support ALL of the given data sources. Note that this does NOT imply that those data sources must be the only data sources that are supported by a bank.
List<String> location = Arrays.asList("location_example"); // List<String> | Comma-separated list of two-letter country codes (ISO 3166 ALPHA-2). If set, then only those banks will be regarded in the search that are located in the specified countries. Notes: Banks which do not have a location set (i.e. international institutes) will ALWAYS be regarded in the search, independent of what you specify for this field. When you pass a country code that doesn't exist in the ISO 3166 ALPHA-2 standard, then the service will respond with 400 BAD_REQUEST.
Boolean isTestBank = true; // Boolean | If specified, then only those banks will be regarded that have the given value (true or false) for their 'isTestBank' field.
Integer page = 1; // Integer | Result page that you want to retrieve.
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by 'id', 'name', 'blz', 'bic' or 'popularity'. The default order for all services is 'id,asc'. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: '/banks?order=name,desc&order=id,asc' will return banks ordered by 'name' (descending), where banks with the same 'name' are ordered by 'id' (ascending). The general format is: 'property[,asc|desc]', with 'asc' being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC). 
try {
    PageableBankList result = apiInstance.getAndSearchAllBanks(ids, search, isSupported, pinsAreVolatile, supportedDataSources, location, isTestBank, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BanksApi#getAndSearchAllBanks");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of bank identifiers. If specified, then only banks whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **search** | **String**| If specified, then only those banks will be contained in the result whose &#39;name&#39;, &#39;blz&#39;, &#39;bic&#39; or &#39;city&#39; contains the given search string (the matching works case-insensitive). If no banks contain the search string in any of the regarded fields, then the result will be an empty list. Note that you may also pass an IBAN in this field, in which case finAPI will try to find the related bank in its database and regard only this bank for the search. Also note: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must apply to a bank in order for it to get included into the result. | [optional]
 **isSupported** | **Boolean**| If specified, then only supported (in case of &#39;true&#39; value) or unsupported (in case of &#39;false&#39; value) banks will be regarded. | [optional]
 **pinsAreVolatile** | **Boolean**| If specified, then only those banks will be regarded that have the given value (true or false) for their &#39;pinsAreVolatile&#39; field. | [optional]
 **supportedDataSources** | [**List&lt;String&gt;**](String.md)| Comma-separated list of data sources. Possible values: WEB_SCRAPER,FINTS_SERVER. If this parameter is specified, then only those banks will be regarded in the search that support ALL of the given data sources. Note that this does NOT imply that those data sources must be the only data sources that are supported by a bank. | [optional]
 **location** | [**List&lt;String&gt;**](String.md)| Comma-separated list of two-letter country codes (ISO 3166 ALPHA-2). If set, then only those banks will be regarded in the search that are located in the specified countries. Notes: Banks which do not have a location set (i.e. international institutes) will ALWAYS be regarded in the search, independent of what you specify for this field. When you pass a country code that doesn&#39;t exist in the ISO 3166 ALPHA-2 standard, then the service will respond with 400 BAD_REQUEST. | [optional]
 **isTestBank** | **Boolean**| If specified, then only those banks will be regarded that have the given value (true or false) for their &#39;isTestBank&#39; field. | [optional]
 **page** | **Integer**| Result page that you want to retrieve. | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by &#39;id&#39;, &#39;name&#39;, &#39;blz&#39;, &#39;bic&#39; or &#39;popularity&#39;. The default order for all services is &#39;id,asc&#39;. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: &#39;/banks?order&#x3D;name,desc&amp;order&#x3D;id,asc&#39; will return banks ordered by &#39;name&#39; (descending), where banks with the same &#39;name&#39; are ordered by &#39;id&#39; (ascending). The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC).  | [optional]

### Return type

[**PageableBankList**](PageableBankList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getBank"></a>
# **getBank**
> Bank getBank(id)

Get a bank

Get a single bank from finAPI&#39;s database of banks. You have to pass the bank&#39;s identifier, and either the authorized user&#39;s access_token, or your client&#39;s access token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BanksApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BanksApi apiInstance = new BanksApi();
Long id = 789L; // Long | Identifier of requested bank
try {
    Bank result = apiInstance.getBank(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BanksApi#getBank");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of requested bank |

### Return type

[**Bank**](Bank.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleBanks"></a>
# **getMultipleBanks**
> BankList getMultipleBanks(ids)

Get multiple banks

Get a list of multiple banks from finAPI&#39;s database of banks. You have to pass a list of bank identifiers, and either the authorized user&#39;s access_token, or your client&#39;s access token. Note that banks whose identifiers do not exist will not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list).&lt;br/&gt;&lt;br/&gt;&lt;b&gt;WARNING&lt;/b&gt;: This service is deprecated and will be removed at some point. If you want to get multiple banks, please instead use the service &#39;Get and search all banks&#39; and pass a comma-separated list of identifiers with the parameter &#39;ids&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.BanksApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

BanksApi apiInstance = new BanksApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested banks
try {
    BankList result = apiInstance.getMultipleBanks(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BanksApi#getMultipleBanks");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested banks |

### Return type

[**BankList**](BankList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

