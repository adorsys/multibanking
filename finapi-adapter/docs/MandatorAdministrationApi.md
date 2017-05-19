# MandatorAdministrationApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteUsers**](MandatorAdministrationApi.md#deleteUsers) | **POST** /api/v1/mandatorAdmin/deleteUsers | Delete users
[**getUserList**](MandatorAdministrationApi.md#getUserList) | **GET** /api/v1/mandatorAdmin/getUserList | Get user list


<a name="deleteUsers"></a>
# **deleteUsers**
> InlineResponse20014 deleteUsers(body)

Delete users

Delete one or several users, which are specified by a given list of identifiers. Must pass the &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115003661827-Difference-between-app-clients-and-mandator-admin-client&#39;&gt;mandator admin client&lt;/a&gt;&#39;s access_token. &lt;br/&gt;&lt;br/&gt;&lt;b&gt;NOTE&lt;/b&gt;: finAPI may fail to delete one (or several, or all) of the specified users. A user cannot get deleted when his data is currently locked by an internal process (for instance, update of a bank connection or transactions categorization). The response contains the identifiers of all users that could not get deleted, and all users that could get deleted, separated in two lists. The mandator admin client can retry the request at a later time for the users who could not get deleted.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.MandatorAdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

MandatorAdministrationApi apiInstance = new MandatorAdministrationApi();
Body10 body = new Body10(); // Body10 | List of user identifiers
try {
    InlineResponse20014 result = apiInstance.deleteUsers(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#deleteUsers");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body10**](Body10.md)| List of user identifiers |

### Return type

[**InlineResponse20014**](InlineResponse20014.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getUserList"></a>
# **getUserList**
> InlineResponse20015 getUserList(minRegistrationDate, maxRegistrationDate, minDeletionDate, maxDeletionDate, minLastActiveDate, maxLastActiveDate, isDeleted, page, perPage, order)

Get user list

&lt;p&gt;Get a list of the users of the mandator that is authorized by the access_token. Must pass the &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115003661827-Difference-between-app-clients-and-mandator-admin-client&#39;&gt;mandator admin client&lt;/a&gt;&#39;s access_token. You can set optional search criteria to get only those users that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.&lt;/p&gt;&lt;p&gt;Note that the original user id is longer available in finAPI once a user has been deleted. Because of this, the userId of deleted users will be a distorted version of the original userId. For example, if the deleted user&#39;s id was originally &#39;user&#39;, then this service will return &#39;uXXr&#39; as the userId.&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.MandatorAdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

MandatorAdministrationApi apiInstance = new MandatorAdministrationApi();
String minRegistrationDate = "minRegistrationDate_example"; // String | Lower bound for a user's registration date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only users whose 'registrationDate' is equal to or later than the given date will be regarded.
String maxRegistrationDate = "maxRegistrationDate_example"; // String | Upper bound for a user's registration date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only users whose 'registrationDate' is equal to or earlier than the given date will be regarded.
String minDeletionDate = "minDeletionDate_example"; // String | Lower bound for a user's deletion date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only users whose 'deletionDate' is not null, and is equal to or later than the given date will be regarded.
String maxDeletionDate = "maxDeletionDate_example"; // String | Upper bound for a user's deletion date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only users whose 'deletionDate' is null, or is equal to or earlier than the given date will be regarded.
String minLastActiveDate = "minLastActiveDate_example"; // String | Lower bound for a user's last active date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only users whose 'lastActiveDate' is not null, and is equal to or later than the given date will be regarded.
String maxLastActiveDate = "maxLastActiveDate_example"; // String | Upper bound for a user's last active date, in the format 'YYYY-MM-DD' (e.g. '2016-01-01'). If specified, then only users whose 'lastActiveDate' is null, or is equal to or earlier than the given date will be regarded.
Boolean isDeleted = true; // Boolean | If NOT specified, then the service will regard both active and deleted users in the search. If set to 'true', then ONLY deleted users will be regarded. If set to 'false', then ONLY active users will be regarded.
Integer page = 1; // Integer | Result page that you want to retrieve
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by userId. The default order for this service is 'userId,asc'. The general format is: 'property[,asc|desc]', with 'asc' being the default value. 
try {
    InlineResponse20015 result = apiInstance.getUserList(minRegistrationDate, maxRegistrationDate, minDeletionDate, maxDeletionDate, minLastActiveDate, maxLastActiveDate, isDeleted, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#getUserList");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **minRegistrationDate** | **String**| Lower bound for a user&#39;s registration date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only users whose &#39;registrationDate&#39; is equal to or later than the given date will be regarded. | [optional]
 **maxRegistrationDate** | **String**| Upper bound for a user&#39;s registration date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only users whose &#39;registrationDate&#39; is equal to or earlier than the given date will be regarded. | [optional]
 **minDeletionDate** | **String**| Lower bound for a user&#39;s deletion date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only users whose &#39;deletionDate&#39; is not null, and is equal to or later than the given date will be regarded. | [optional]
 **maxDeletionDate** | **String**| Upper bound for a user&#39;s deletion date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only users whose &#39;deletionDate&#39; is null, or is equal to or earlier than the given date will be regarded. | [optional]
 **minLastActiveDate** | **String**| Lower bound for a user&#39;s last active date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only users whose &#39;lastActiveDate&#39; is not null, and is equal to or later than the given date will be regarded. | [optional]
 **maxLastActiveDate** | **String**| Upper bound for a user&#39;s last active date, in the format &#39;YYYY-MM-DD&#39; (e.g. &#39;2016-01-01&#39;). If specified, then only users whose &#39;lastActiveDate&#39; is null, or is equal to or earlier than the given date will be regarded. | [optional]
 **isDeleted** | **Boolean**| If NOT specified, then the service will regard both active and deleted users in the search. If set to &#39;true&#39;, then ONLY deleted users will be regarded. If set to &#39;false&#39;, then ONLY active users will be regarded. | [optional]
 **page** | **Integer**| Result page that you want to retrieve | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by userId. The default order for this service is &#39;userId,asc&#39;. The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value.  | [optional]

### Return type

[**InlineResponse20015**](InlineResponse20015.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

