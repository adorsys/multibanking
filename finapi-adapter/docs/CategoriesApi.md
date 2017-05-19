# CategoriesApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createCategory**](CategoriesApi.md#createCategory) | **POST** /api/v1/categories | Create a new category
[**deleteAllCategories**](CategoriesApi.md#deleteAllCategories) | **DELETE** /api/v1/categories | Delete all categories
[**deleteCategory**](CategoriesApi.md#deleteCategory) | **DELETE** /api/v1/categories/{id} | Delete a category
[**getAndSearchAllCategories**](CategoriesApi.md#getAndSearchAllCategories) | **GET** /api/v1/categories | Get and search all categories
[**getCashFlows**](CategoriesApi.md#getCashFlows) | **GET** /api/v1/categories/cashFlows | Get cash flows
[**getCategory**](CategoriesApi.md#getCategory) | **GET** /api/v1/categories/{id} | Get a category
[**getMultipleCategories**](CategoriesApi.md#getMultipleCategories) | **GET** /api/v1/categories/{ids} | Get multiple categories


<a name="createCategory"></a>
# **createCategory**
> InlineResponse2008Categories createCategory(body)

Create a new category

Create a new custom category for the authorized user. Must pass the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
Body6 body = new Body6(); // Body6 | Parameters of the new category
try {
    InlineResponse2008Categories result = apiInstance.createCategory(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#createCategory");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body6**](Body6.md)| Parameters of the new category | [optional]

### Return type

[**InlineResponse2008Categories**](InlineResponse2008Categories.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteAllCategories"></a>
# **deleteAllCategories**
> InlineResponse2001 deleteAllCategories()

Delete all categories

Delete all custom categories of the user that is authorized by the access_token. Must pass the user&#39;s access_token. Note that this deletes both parent categories as well as any related sub-categories.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
try {
    InlineResponse2001 result = apiInstance.deleteAllCategories();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#deleteAllCategories");
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

<a name="deleteCategory"></a>
# **deleteCategory**
> deleteCategory(id)

Delete a category

Delete a single category of the user that is authorized by the access_token. Must pass the user&#39;s access_token. Note that you can only delete user-custom categories (category&#39;s where the &#39;isCustom&#39; flag is true). Also note that when deleting a parent category, all its sub-categories will be deleted as well.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
Long id = 789L; // Long | Category identifier
try {
    apiInstance.deleteCategory(id);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#deleteCategory");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Category identifier |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAndSearchAllCategories"></a>
# **getAndSearchAllCategories**
> InlineResponse2008 getAndSearchAllCategories(ids, search, isCustom, page, perPage, order)

Get and search all categories

Get a list of all global finAPI categories as well as all custom categories of the authorized user. Must pass the user&#39;s access_token. You can set optional search criteria to get only those categories that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of category identifiers. If specified, then only categories whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String search = "search_example"; // String | If specified, then only those categories will be contained in the result whose 'name' contains the given search string (the matching works case-insensitive). If no categories contain the search string in their name, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the name in order for a category to get included into the result.
Boolean isCustom = true; // Boolean | If specified, then the result will contain only categories that are either finAPI global (in case of value 'false'), or only categories that have been created by the authorized user (in case of value 'true').
Integer page = 1; // Integer | Result page that you want to retrieve.
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by 'id', 'name' and 'isCustom'. The default order is 'id,asc'. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: '/categories?order=isCustom,desc&order=name' will return all custom categories followed by all default categories. Both groups are ordered ascending by name. The general format is: 'property[,asc|desc]', with 'asc' being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC). 
try {
    InlineResponse2008 result = apiInstance.getAndSearchAllCategories(ids, search, isCustom, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#getAndSearchAllCategories");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of category identifiers. If specified, then only categories whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **search** | **String**| If specified, then only those categories will be contained in the result whose &#39;name&#39; contains the given search string (the matching works case-insensitive). If no categories contain the search string in their name, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the name in order for a category to get included into the result. | [optional]
 **isCustom** | **Boolean**| If specified, then the result will contain only categories that are either finAPI global (in case of value &#39;false&#39;), or only categories that have been created by the authorized user (in case of value &#39;true&#39;). | [optional]
 **page** | **Integer**| Result page that you want to retrieve. | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by &#39;id&#39;, &#39;name&#39; and &#39;isCustom&#39;. The default order is &#39;id,asc&#39;. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: &#39;/categories?order&#x3D;isCustom,desc&amp;order&#x3D;name&#39; will return all custom categories followed by all default categories. Both groups are ordered ascending by name. The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC).  | [optional]

### Return type

[**InlineResponse2008**](InlineResponse2008.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getCashFlows"></a>
# **getCashFlows**
> InlineResponse2009 getCashFlows(search, counterpart, accountIds, minBankBookingDate, maxBankBookingDate, minFinapiBookingDate, maxFinapiBookingDate, minAmount, maxAmount, direction, labelIds, categoryIds, isNew, minImportDate, maxImportDate, includeSubCashFlows, order)

Get cash flows

Get the cash flow(s) (&#x3D; total income, spending, and balance) for one or several categories. You can specify various criteria such as the time period to calculate the cash flows for, or what categories to do the calculations for. Note that the cash flow for a category may include the cash flows for all of its sub-categories, or not include it, depending on the &#39;includeSubCashFlows&#39; setting. Must pass the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
String search = "search_example"; // String | If specified, then only transactions that contain the search term in their purpose or counterpart fields will be contained in the result. Note that the search is case insensitive.
String counterpart = "counterpart_example"; // String | The counterpart is the person or institution that received your payment, or that you made the payment to. If this parameter is specified, then only transactions that contain the given term in one (or more) of their counterpart fields ('counterpartName', 'counterpartAccountNumber', 'counterpartIban', 'counterpartBic' or 'counterpartBlz') will be contained in the result. Note that the search is case insensitive.
List<Long> accountIds = Arrays.asList(56L); // List<Long> | A comma-separated list of account identifiers. If specified, then only transactions that relate to the given accounts will be regarded. If not specified, then all accounts will be regarded.
String minBankBookingDate = "minBankBookingDate_example"; // String | Lower bound for a transaction's booking date as returned by the bank (= original booking date), in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'bankBookingDate' is equal to or later than the given date will be regarded.
String maxBankBookingDate = "maxBankBookingDate_example"; // String | Upper bound for a transaction's booking date as returned by the bank (= original booking date), in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'bankBookingDate' is equal to or earlier than the given date will be regarded.
String minFinapiBookingDate = "minFinapiBookingDate_example"; // String | Lower bound for a transaction's booking date as set by finAPI, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). For details about the meaning of the finAPI booking date, please see the field's documentation in the service's response.
String maxFinapiBookingDate = "maxFinapiBookingDate_example"; // String | Upper bound for a transaction's booking date as set by finAPI, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). For details about the meaning of the finAPI booking date, please see the field's documentation in the service's response.
BigDecimal minAmount = new BigDecimal(); // BigDecimal | If specified, then only transactions whose amount is equal to or greater than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95
BigDecimal maxAmount = new BigDecimal(); // BigDecimal | If specified, then only transactions whose amount is equal to or less than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95
String direction = "all"; // String | If specified, then only transactions with the given direction(s) will be regarded. Use 'income' for regarding only received payments (amount >= 0), 'spending' for regarding only outgoing payments (amount < 0), or 'all' to regard both directions. If not specified, the direction defaults to 'all'.
List<Long> labelIds = Arrays.asList(56L); // List<Long> | A comma-separated list of label identifiers. If specified, then only transactions that have been marked with at least one of the given labels will be contained in the result.
List<Long> categoryIds = Arrays.asList(56L); // List<Long> | If specified, then the result will contain only those cash flows that relate to the given categories. Note that the cash flow for a category may include/exclude the cash flows of its sub-categories, depending on the 'includeSubCashFlows' setting. To include the cash flow of not categorized transactions, pass the value '0' as categoryId. Note: When this parameter is NOT set, then the result will contain a cash flow for all categories that have transactions associated to them (this includes the 'null'-category for the cash flow of not categorized transactions), more precisely: transactions that fulfill the filter criteria. Categories that have no associated transactions according to the filter criteria will not appear in the result. However, when you specify this parameter, then all specified categories will have a cash flow entry in the result, even if there are no associated transactions for the category (the cash flow will have income, spending and balance all set to zero).
Boolean isNew = true; // Boolean | If specified, then only transactions that have their 'isNew' flag set to true/false will be regarded for the cash flow calculations.
String minImportDate = "minImportDate_example"; // String | Lower bound for a transaction's import date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'importDate' is equal to or later than the given date will be regarded.
String maxImportDate = "maxImportDate_example"; // String | Upper bound for a transaction's import date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only transactions whose 'importDate' is equal to or earlier than the given date will be regarded.
Boolean includeSubCashFlows = true; // Boolean | If it is true, then the income, spending and balance of a main category results from all transactions that have either this (main) category or any of its subcategories assigned (of course all transactions depends from the other filtering settings); If it is false, then the income, spending and balance of a main category only results from the transactions that have exactly this (main) category assigned. Default value for this parameter is 'true'.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by income, spending, balance, category.id or category.name. The default order for this service is 'category.id,asc'. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: '/cashFlows?order=income,desc&order=spending,asc&balance,desc' will return as first result the category with the highest income. If two categories have the same income, it returns the category with the highest spending first (because spending is a negative value) and so on. The general format is: 'property[,asc|desc]', with 'asc' being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC). 
try {
    InlineResponse2009 result = apiInstance.getCashFlows(search, counterpart, accountIds, minBankBookingDate, maxBankBookingDate, minFinapiBookingDate, maxFinapiBookingDate, minAmount, maxAmount, direction, labelIds, categoryIds, isNew, minImportDate, maxImportDate, includeSubCashFlows, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#getCashFlows");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **search** | **String**| If specified, then only transactions that contain the search term in their purpose or counterpart fields will be contained in the result. Note that the search is case insensitive. | [optional]
 **counterpart** | **String**| The counterpart is the person or institution that received your payment, or that you made the payment to. If this parameter is specified, then only transactions that contain the given term in one (or more) of their counterpart fields (&#39;counterpartName&#39;, &#39;counterpartAccountNumber&#39;, &#39;counterpartIban&#39;, &#39;counterpartBic&#39; or &#39;counterpartBlz&#39;) will be contained in the result. Note that the search is case insensitive. | [optional]
 **accountIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of account identifiers. If specified, then only transactions that relate to the given accounts will be regarded. If not specified, then all accounts will be regarded. | [optional]
 **minBankBookingDate** | **String**| Lower bound for a transaction&#39;s booking date as returned by the bank (&#x3D; original booking date), in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;bankBookingDate&#39; is equal to or later than the given date will be regarded. | [optional]
 **maxBankBookingDate** | **String**| Upper bound for a transaction&#39;s booking date as returned by the bank (&#x3D; original booking date), in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;bankBookingDate&#39; is equal to or earlier than the given date will be regarded. | [optional]
 **minFinapiBookingDate** | **String**| Lower bound for a transaction&#39;s booking date as set by finAPI, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). For details about the meaning of the finAPI booking date, please see the field&#39;s documentation in the service&#39;s response. | [optional]
 **maxFinapiBookingDate** | **String**| Upper bound for a transaction&#39;s booking date as set by finAPI, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). For details about the meaning of the finAPI booking date, please see the field&#39;s documentation in the service&#39;s response. | [optional]
 **minAmount** | **BigDecimal**| If specified, then only transactions whose amount is equal to or greater than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95 | [optional]
 **maxAmount** | **BigDecimal**| If specified, then only transactions whose amount is equal to or less than the given amount will be regarded. Can contain a positive or negative number with at most two decimal places. Examples: -300.12, or 90.95 | [optional]
 **direction** | **String**| If specified, then only transactions with the given direction(s) will be regarded. Use &#39;income&#39; for regarding only received payments (amount &gt;&#x3D; 0), &#39;spending&#39; for regarding only outgoing payments (amount &lt; 0), or &#39;all&#39; to regard both directions. If not specified, the direction defaults to &#39;all&#39;. | [optional] [default to all] [enum: all, income, spending]
 **labelIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of label identifiers. If specified, then only transactions that have been marked with at least one of the given labels will be contained in the result. | [optional]
 **categoryIds** | [**List&lt;Long&gt;**](Long.md)| If specified, then the result will contain only those cash flows that relate to the given categories. Note that the cash flow for a category may include/exclude the cash flows of its sub-categories, depending on the &#39;includeSubCashFlows&#39; setting. To include the cash flow of not categorized transactions, pass the value &#39;0&#39; as categoryId. Note: When this parameter is NOT set, then the result will contain a cash flow for all categories that have transactions associated to them (this includes the &#39;null&#39;-category for the cash flow of not categorized transactions), more precisely: transactions that fulfill the filter criteria. Categories that have no associated transactions according to the filter criteria will not appear in the result. However, when you specify this parameter, then all specified categories will have a cash flow entry in the result, even if there are no associated transactions for the category (the cash flow will have income, spending and balance all set to zero). | [optional]
 **isNew** | **Boolean**| If specified, then only transactions that have their &#39;isNew&#39; flag set to true/false will be regarded for the cash flow calculations. | [optional]
 **minImportDate** | **String**| Lower bound for a transaction&#39;s import date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;importDate&#39; is equal to or later than the given date will be regarded. | [optional]
 **maxImportDate** | **String**| Upper bound for a transaction&#39;s import date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only transactions whose &#39;importDate&#39; is equal to or earlier than the given date will be regarded. | [optional]
 **includeSubCashFlows** | **Boolean**| If it is true, then the income, spending and balance of a main category results from all transactions that have either this (main) category or any of its subcategories assigned (of course all transactions depends from the other filtering settings); If it is false, then the income, spending and balance of a main category only results from the transactions that have exactly this (main) category assigned. Default value for this parameter is &#39;true&#39;. | [optional] [default to true]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by income, spending, balance, category.id or category.name. The default order for this service is &#39;category.id,asc&#39;. You can also order by multiple properties. In that case the order of the parameters passed is important. Example: &#39;/cashFlows?order&#x3D;income,desc&amp;order&#x3D;spending,asc&amp;balance,desc&#39; will return as first result the category with the highest income. If two categories have the same income, it returns the category with the highest spending first (because spending is a negative value) and so on. The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value. Please note that ordering by multiple fields is not supported in our swagger frontend, but you can test this feature with any HTTP tool of your choice (e.g. postman or DHC).  | [optional]

### Return type

[**InlineResponse2009**](InlineResponse2009.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getCategory"></a>
# **getCategory**
> InlineResponse2008Categories getCategory(id)

Get a category

Get a single category that is either a global finAPI category or a custom category of the authorized user. Must pass the category&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
Long id = 789L; // Long | Category identifier
try {
    InlineResponse2008Categories result = apiInstance.getCategory(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#getCategory");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Category identifier |

### Return type

[**InlineResponse2008Categories**](InlineResponse2008Categories.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleCategories"></a>
# **getMultipleCategories**
> InlineResponse20010 getMultipleCategories(ids)

Get multiple categories

Get a list of multiple categories that are either a global finAPI category or a custom category of the authorized user. Must pass the categories&#39; identifiers and the user&#39;s access_token. Categories whose identifiers do not exist or that relate to a different user not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list). WARNING: This service is deprecated and will be removed at some point. If you want to get multiple categories, please instead use the service &#39;Get and search all categories&#39; and pass a comma-separated list of identifiers as a parameter &#39;ids&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.CategoriesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

CategoriesApi apiInstance = new CategoriesApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested categories
try {
    InlineResponse20010 result = apiInstance.getMultipleCategories(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CategoriesApi#getMultipleCategories");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested categories |

### Return type

[**InlineResponse20010**](InlineResponse20010.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

