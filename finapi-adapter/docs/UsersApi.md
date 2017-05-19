# UsersApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createUser**](UsersApi.md#createUser) | **POST** /api/v1/users | Create a new user
[**deleteAuthorizedUser**](UsersApi.md#deleteAuthorizedUser) | **DELETE** /api/v1/users | Delete the authorized user
[**deleteUnverifiedUser**](UsersApi.md#deleteUnverifiedUser) | **DELETE** /api/v1/users/{userId} | Delete an unverified user
[**editAuthorizedUser**](UsersApi.md#editAuthorizedUser) | **PATCH** /api/v1/users | Edit the authorized user
[**executePasswordChange**](UsersApi.md#executePasswordChange) | **POST** /api/v1/users/executePasswordChange | Execute password change
[**getAuthorizedUser**](UsersApi.md#getAuthorizedUser) | **GET** /api/v1/users | Get the authorized user
[**getVerificationStatus**](UsersApi.md#getVerificationStatus) | **GET** /api/v1/users/verificationStatus | Get a user&#39;s verification status
[**requestPasswordChange**](UsersApi.md#requestPasswordChange) | **POST** /api/v1/users/requestPasswordChange | Request password change
[**verifyUser**](UsersApi.md#verifyUser) | **POST** /api/v1/users/verify/{userId} | Verify a user


<a name="createUser"></a>
# **createUser**
> InlineResponse20021 createUser(body)

Create a new user

&lt;p&gt;Create a new user. Must pass your global (i.e. client) access_token. &lt;/p&gt;&lt;p&gt;This service returns the user&#39;s password as plain text. The automatic update of the user&#39;s bank connections is disabled by default for any new user. User identifiers are regarded case-insensitive by finAPI.&lt;/p&gt;&lt;p&gt;Please note that finAPI generally has a restricted set of allowed characters for input fields. You can find the allowed characters &lt;a href &#x3D; \&quot;https://finapi.zendesk.com/hc/en-us/articles/222013148-What-symbols-are-allowed-in-finAPI-\&quot;&gt;here&lt;/a&gt;. If a field does not explicitly specify a set of allowed characters, then these are the characters that are allowed for the field. Some fields may specify a different set of characters, in which case this will be documented for the field (like for the &#39;id&#39; field in this service).&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
Body18 body = new Body18(); // Body18 | User's details
try {
    InlineResponse20021 result = apiInstance.createUser(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#createUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body18**](Body18.md)| User&#39;s details |

### Return type

[**InlineResponse20021**](InlineResponse20021.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteAuthorizedUser"></a>
# **deleteAuthorizedUser**
> deleteAuthorizedUser()

Delete the authorized user

Delete the authorized user. Must pass the user&#39;s access_token. ATTENTION: This deletes the user including all of his bank connections, accounts, balance data and transactions! THIS PROCESS CANNOT BE UNDONE! Note that a user cannot get deleted while any of his bank connections are currently busy (in the process of import, update, or transactions categorization). &lt;p&gt;Note: finAPI will send a notification about the deletion of the user to each of your clients that has a user synchronization callback URL set in its configuration. This also includes the client that is performing this request.&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
try {
    apiInstance.deleteAuthorizedUser();
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#deleteAuthorizedUser");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteUnverifiedUser"></a>
# **deleteUnverifiedUser**
> deleteUnverifiedUser(userId)

Delete an unverified user

Delete an unverified user. Must pass your global (i.e. client) access_token.&lt;p&gt;Note: finAPI will send a notification about the deletion of the user to each of your clients that has a user synchronization callback URL set in its configuration. This also includes the client that is performing this request.&lt;/p&gt;Also note that finAPI regards user identifiers case-insensitive.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
String userId = "userId_example"; // String | 
try {
    apiInstance.deleteUnverifiedUser(userId);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#deleteUnverifiedUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**|  |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="editAuthorizedUser"></a>
# **editAuthorizedUser**
> InlineResponse20021 editAuthorizedUser(body)

Edit the authorized user

Edit the authorized user&#39;s data and settings. Must pass the user&#39;s access_token. Pass an empty string (but not null) to unset either the email or phone number. At least one field must have a non-null value in the request body. This service returns the user&#39;s password in distorted form.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
Body19 body = new Body19(); // Body19 | User's details
try {
    InlineResponse20021 result = apiInstance.editAuthorizedUser(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#editAuthorizedUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body19**](Body19.md)| User&#39;s details |

### Return type

[**InlineResponse20021**](InlineResponse20021.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="executePasswordChange"></a>
# **executePasswordChange**
> executePasswordChange(body)

Execute password change

Change the password of a user. Must pass your global (i.e. client) access_token. Note: When changing the password of a user, all tokens that have been handed out for that user (for whatever client) will be revoked! Also note that finAPI regards user identifiers case-insensitive.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
Body20 body = new Body20(); // Body20 | 
try {
    apiInstance.executePasswordChange(body);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#executePasswordChange");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body20**](Body20.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAuthorizedUser"></a>
# **getAuthorizedUser**
> InlineResponse20021 getAuthorizedUser()

Get the authorized user

Get the authorized user&#39;s data. Must pass the user&#39;s access_token. Only the authorized user can get his data, i.e. his access_token must be used. Note that each symbol of the password is distorted with an &#39;X&#39; character, i.e. if the user&#39;s password is &#39;12345&#39;, the service will return the string &#39;XXXXX&#39;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
try {
    InlineResponse20021 result = apiInstance.getAuthorizedUser();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#getAuthorizedUser");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**InlineResponse20021**](InlineResponse20021.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getVerificationStatus"></a>
# **getVerificationStatus**
> InlineResponse20023 getVerificationStatus(userId)

Get a user&#39;s verification status

Get the verification status of the requested user. Must pass your global (i.e. client) access_token. Note that finAPI regards user identifiers case-insensitive.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
String userId = "userId_example"; // String | User's identifier
try {
    InlineResponse20023 result = apiInstance.getVerificationStatus(userId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#getVerificationStatus");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| User&#39;s identifier |

### Return type

[**InlineResponse20023**](InlineResponse20023.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="requestPasswordChange"></a>
# **requestPasswordChange**
> InlineResponse20022 requestPasswordChange(body)

Request password change

Request password change for a user. Must pass your global (i.e. client) access_token. Note that finAPI regards user identifiers case-insensitive.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
Body21 body = new Body21(); // Body21 | 
try {
    InlineResponse20022 result = apiInstance.requestPasswordChange(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#requestPasswordChange");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body21**](Body21.md)|  | [optional]

### Return type

[**InlineResponse20022**](InlineResponse20022.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="verifyUser"></a>
# **verifyUser**
> verifyUser(userId)

Verify a user

Verify a user. Must pass your global (i.e. client) access_token. Note that finAPI regards user identifiers case-insensitive.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.UsersApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

UsersApi apiInstance = new UsersApi();
String userId = "userId_example"; // String | User's identifier
try {
    apiInstance.verifyUser(userId);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#verifyUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| User&#39;s identifier |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

