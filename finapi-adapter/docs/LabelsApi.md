# LabelsApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createLabel**](LabelsApi.md#createLabel) | **POST** /api/v1/labels | Create a new label
[**deleteAllLabels**](LabelsApi.md#deleteAllLabels) | **DELETE** /api/v1/labels | Delete all labels
[**deleteLabel**](LabelsApi.md#deleteLabel) | **DELETE** /api/v1/labels/{id} | Delete a label
[**editLabel**](LabelsApi.md#editLabel) | **PATCH** /api/v1/labels/{id} | Edit a label
[**getAndSearchAllLabels**](LabelsApi.md#getAndSearchAllLabels) | **GET** /api/v1/labels | Get and search all labels
[**getLabel**](LabelsApi.md#getLabel) | **GET** /api/v1/labels/{id} | Get a label
[**getMultipleLabels**](LabelsApi.md#getMultipleLabels) | **GET** /api/v1/labels/{ids} | Get multiple labels


<a name="createLabel"></a>
# **createLabel**
> InlineResponse20012Labels createLabel(body)

Create a new label

Create a new label for a specific user. Must pass the new label&#39;s name and the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Users can create labels to flag transactions (see method PATCH /transactions), with the goal of collecting and getting an overview of all transactions of a certain &#39;type&#39;. In this sense, labels are similar to transaction categories. However, labels are supposed to depict more of an implicit meaning of a transaction. For instance, a user might want to assign a flag to a transaction that reminds him that he can offset it against tax. At the same time, the category of the transactions might be something like &#39;insurance&#39;, which is a more &#39;fact-based&#39;, or &#39;objective&#39; way of typing the transaction. Despite this semantic difference between categories and labels, there is also the difference that a transaction can be assigned multiple labels at the same time (while in contrast it can have just a single category).

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
Body8 body = new Body8(); // Body8 | Label's name
try {
    InlineResponse20012Labels result = apiInstance.createLabel(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#createLabel");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body8**](Body8.md)| Label&#39;s name |

### Return type

[**InlineResponse20012Labels**](InlineResponse20012Labels.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteAllLabels"></a>
# **deleteAllLabels**
> InlineResponse2001 deleteAllLabels()

Delete all labels

Delete all labels of the user that is authorized by the access_token. Must pass the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
try {
    InlineResponse2001 result = apiInstance.deleteAllLabels();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#deleteAllLabels");
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

<a name="deleteLabel"></a>
# **deleteLabel**
> deleteLabel(id)

Delete a label

Delete a single label of the user that is authorized by the access_token. Must pass the label&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
Long id = 789L; // Long | Identifier of the label to delete
try {
    apiInstance.deleteLabel(id);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#deleteLabel");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of the label to delete |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editLabel"></a>
# **editLabel**
> InlineResponse20012Labels editLabel(id, body)

Edit a label

Change the name of a label of the user that is authorized by the access_token. Must pass the label&#39;s identifier, the label&#39;s new name and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
Long id = 789L; // Long | Label's identifier
Body9 body = new Body9(); // Body9 | Label's new name
try {
    InlineResponse20012Labels result = apiInstance.editLabel(id, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#editLabel");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Label&#39;s identifier |
 **body** | [**Body9**](Body9.md)| Label&#39;s new name |

### Return type

[**InlineResponse20012Labels**](InlineResponse20012Labels.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAndSearchAllLabels"></a>
# **getAndSearchAllLabels**
> InlineResponse20012 getAndSearchAllLabels(ids, search, page, perPage, order)

Get and search all labels

Get labels of the user that is authorized by the access_token. Must pass the user&#39;s access_token. You can set optional search criteria to get only those labels that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of label identifiers. If specified, then only labels whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String search = "search_example"; // String | If specified, then only those labels will be contained in the result whose 'name' contains the given search string (the matching works case-insensitive). If no labels contain the search string in their name, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the name in order for a label to get included into the result.
Integer page = 1; // Integer | Result page that you want to retrieve
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by id or name. The default order for all services is 'id,asc'. Since both fields (id and name) are unique, ordering by multiple fields is pointless. The general format is: 'property[,asc|desc]', with 'asc' being the default value. 
try {
    InlineResponse20012 result = apiInstance.getAndSearchAllLabels(ids, search, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#getAndSearchAllLabels");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of label identifiers. If specified, then only labels whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **search** | **String**| If specified, then only those labels will be contained in the result whose &#39;name&#39; contains the given search string (the matching works case-insensitive). If no labels contain the search string in their name, then the result will be an empty list. NOTE: If the given search string consists of several terms (separated by whitespace), then ALL of these terms must be contained in the name in order for a label to get included into the result. | [optional]
 **page** | **Integer**| Result page that you want to retrieve | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by id or name. The default order for all services is &#39;id,asc&#39;. Since both fields (id and name) are unique, ordering by multiple fields is pointless. The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value.  | [optional]

### Return type

[**InlineResponse20012**](InlineResponse20012.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getLabel"></a>
# **getLabel**
> InlineResponse20012Labels getLabel(id)

Get a label

Get a single label of the user that is authorized by the access_token. Must pass the label&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
Long id = 789L; // Long | Identifier of requested label
try {
    InlineResponse20012Labels result = apiInstance.getLabel(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#getLabel");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of requested label |

### Return type

[**InlineResponse20012Labels**](InlineResponse20012Labels.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getMultipleLabels"></a>
# **getMultipleLabels**
> InlineResponse20013 getMultipleLabels(ids)

Get multiple labels

Get a list of multiple labels of the user that is authorized by the access_token.Must pass the labels&#39; identifiers and the user&#39;s access_token. Identifiers that do not exist or do not relate to the authorized user will not be contained in the result (If this applies to all of the given identifiers, then the result will be an empty list). WARNING: This service is deprecated and will be removed at some point. If you want to get multiple labels, please instead use the service &#39;Get all labels&#39; and pass a comma-separated list of identifiers as a parameter &#39;ids&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.LabelsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

LabelsApi apiInstance = new LabelsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | Comma-separated list of identifiers of requested labels
try {
    InlineResponse20013 result = apiInstance.getMultipleLabels(ids);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LabelsApi#getMultipleLabels");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| Comma-separated list of identifiers of requested labels |

### Return type

[**InlineResponse20013**](InlineResponse20013.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

