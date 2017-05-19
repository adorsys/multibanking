# NotificationRulesApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createNotificationRule**](NotificationRulesApi.md#createNotificationRule) | **POST** /api/v1/notificationRules | Create a new notification rule
[**deleteAllNotificationRules**](NotificationRulesApi.md#deleteAllNotificationRules) | **DELETE** /api/v1/notificationRules | Delete all notification rules
[**deleteNotificationRule**](NotificationRulesApi.md#deleteNotificationRule) | **DELETE** /api/v1/notificationRules/{id} | Delete a notification rule
[**getAndSearchAllNotificationRules**](NotificationRulesApi.md#getAndSearchAllNotificationRules) | **GET** /api/v1/notificationRules | Get and search all notification rules
[**getNotificationRule**](NotificationRulesApi.md#getNotificationRule) | **GET** /api/v1/notificationRules/{id} | Get a notification rule


<a name="createNotificationRule"></a>
# **createNotificationRule**
> InlineResponse20016NotificationRules createNotificationRule(body)

Create a new notification rule

Create a new notification rule for a specific user. Must pass the user&#39;s access_token.&lt;br/&gt;&lt;br/&gt;Setting up notification rules for a user allows your client application to get notified about changes in the user&#39;s data, e.g. when new transactions were downloaded, an account&#39;s balance has changed, or the user&#39;s banking credentials are no longer correct. Note that currently, this feature is implemented only for finAPI&#39;s automatic batch update, i.e. notification rules are only relevant when the user has activated the automatic updates (and when the automatic batch update is activated in general for your client).&lt;br/&gt;&lt;br/&gt;There are different kinds of notification rules. The kind of a rule is depicted by the &#39;triggerEvent&#39;. The trigger event specifies what data you have to pass when creating a rule (specifically, the contents of the &#39;params&#39; field), on which events finAPI will send notifications to your client application, as well as what data is contained in those notifications. The specifics of the different trigger events are documented in the following article on our Dev Portal: &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/232324608-How-to-create-notification-rules-and-receive-notifications&#39;&gt;How to create notification rules and receive notifications&lt;/a&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.NotificationRulesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

NotificationRulesApi apiInstance = new NotificationRulesApi();
Body11 body = new Body11(); // Body11 | Notification rule parameters
try {
    InlineResponse20016NotificationRules result = apiInstance.createNotificationRule(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling NotificationRulesApi#createNotificationRule");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body11**](Body11.md)| Notification rule parameters |

### Return type

[**InlineResponse20016NotificationRules**](InlineResponse20016NotificationRules.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteAllNotificationRules"></a>
# **deleteAllNotificationRules**
> InlineResponse2001 deleteAllNotificationRules()

Delete all notification rules

Delete all notification rules of the user that is authorized by the access_token. Must pass the user&#39;s access_token. 

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.NotificationRulesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

NotificationRulesApi apiInstance = new NotificationRulesApi();
try {
    InlineResponse2001 result = apiInstance.deleteAllNotificationRules();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling NotificationRulesApi#deleteAllNotificationRules");
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

<a name="deleteNotificationRule"></a>
# **deleteNotificationRule**
> deleteNotificationRule(id)

Delete a notification rule

Delete a single notification rule of the user that is authorized by the access_token. Must pass the notification rule&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.NotificationRulesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

NotificationRulesApi apiInstance = new NotificationRulesApi();
Long id = 789L; // Long | Identifier of the notification rule to delete
try {
    apiInstance.deleteNotificationRule(id);
} catch (ApiException e) {
    System.err.println("Exception when calling NotificationRulesApi#deleteNotificationRule");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of the notification rule to delete |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAndSearchAllNotificationRules"></a>
# **getAndSearchAllNotificationRules**
> InlineResponse20016 getAndSearchAllNotificationRules(ids, triggerEvent, includeDetails)

Get and search all notification rules

Get notification rules of the user that is authorized by the access_token. Must pass the user&#39;s access_token. You can set optional search criteria to get only those notification rules that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.NotificationRulesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

NotificationRulesApi apiInstance = new NotificationRulesApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of notification rule identifiers. If specified, then only notification rules whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
String triggerEvent = "triggerEvent_example"; // String | If specified, then only notification rules with given trigger event will be regarded.
Boolean includeDetails = true; // Boolean | If specified, then only notification rules that include or not include details will be regarded.
try {
    InlineResponse20016 result = apiInstance.getAndSearchAllNotificationRules(ids, triggerEvent, includeDetails);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling NotificationRulesApi#getAndSearchAllNotificationRules");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of notification rule identifiers. If specified, then only notification rules whose identifier match any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **triggerEvent** | **String**| If specified, then only notification rules with given trigger event will be regarded. | [optional] [enum: NEW_ACCOUNT_BALANCE, NEW_TRANSACTIONS, BANK_LOGIN_ERROR, FOREIGN_MONEY_TRANSFER, LOW_ACCOUNT_BALANCE, HIGH_TRANSACTION_AMOUNT, CATEGORY_CASH_FLOW]
 **includeDetails** | **Boolean**| If specified, then only notification rules that include or not include details will be regarded. | [optional]

### Return type

[**InlineResponse20016**](InlineResponse20016.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getNotificationRule"></a>
# **getNotificationRule**
> InlineResponse20016NotificationRules getNotificationRule(id)

Get a notification rule

Get a single notification rule of the user that is authorized by the access_token. Must pass the notification rule&#39;s identifier and the user&#39;s access_token.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.NotificationRulesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

NotificationRulesApi apiInstance = new NotificationRulesApi();
Long id = 789L; // Long | Identifier of requested notification rule
try {
    InlineResponse20016NotificationRules result = apiInstance.getNotificationRule(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling NotificationRulesApi#getNotificationRule");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of requested notification rule |

### Return type

[**InlineResponse20016NotificationRules**](InlineResponse20016NotificationRules.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

