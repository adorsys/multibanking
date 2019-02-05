# UsersApi

All URIs are relative to *https://localhost*

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
> User createUser(body)

Create a new user

&lt;p&gt;Create a new user. Must pass your global (i.e. client) access_token. &lt;/p&gt;&lt;p&gt;This service returns the user&#39;s password as plain text. &lt;/p&gt;&lt;p&gt;The automatic update of the user&#39;s bank connections is disabled by default for any new user. User identifiers are regarded case-insensitive by finAPI.&lt;/p&gt;&lt;p&gt;Please note that finAPI generally has a restricted set of allowed characters for input fields. You can find the allowed characters &lt;a href &#x3D; \&quot;https://finapi.zendesk.com/hc/en-us/articles/222013148-What-symbols-are-allowed-in-finAPI-\&quot;&gt;here&lt;/a&gt;. If a field does not explicitly specify a set of allowed characters, then these are the characters that are allowed for the field. Some fields may specify a different set of characters, in which case this will be documented for the field (like for the &#39;id&#39; field in this service).&lt;/p&gt;

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
UserCreateParams body = new UserCreateParams(); // UserCreateParams | User's details
try {
    User result = apiInstance.createUser(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#createUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**UserCreateParams**](UserCreateParams.md)| User&#39;s details |

### Return type

[**User**](User.md)

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

Delete an unverified user. Must pass your global (i.e. client) access_token.&lt;br/&gt;&lt;br/&gt;Notes:&lt;br/&gt;&amp;bull; Unverified users can only exist if the field &#39;isUserAutoVerificationEnabled&#39; (see Client Configuration Resource) is set to &#39;false&#39; (or had been false at some point in the past).&lt;br/&gt;&amp;bull; finAPI will send a notification about the deletion of the user to each of your clients that has a user synchronization callback URL set in its configuration. This also includes the client that is performing this request.&lt;br/&gt;&amp;bull; finAPI regards user identifiers case-insensitive.

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
> User editAuthorizedUser(body)

Edit the authorized user

Edit the authorized user&#39;s data and settings. Must pass the user&#39;s access_token. Pass an empty string (but not null) to unset either the email or phone number. At least one field must have a non-null value in the request body. This service returns the user&#39;s password as &#39;XXXXX&#39;.

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
UserUpdateParams body = new UserUpdateParams(); // UserUpdateParams | User's details
try {
    User result = apiInstance.editAuthorizedUser(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#editAuthorizedUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**UserUpdateParams**](UserUpdateParams.md)| User&#39;s details |

### Return type

[**User**](User.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="executePasswordChange"></a>
# **executePasswordChange**
> executePasswordChange(body)

Execute password change

Change the password of a user. Must pass your global (i.e. client) access_token.&lt;br/&gt;&lt;br/&gt;Note: When changing the password of a user, all tokens that have been handed out for that user (for whatever client) will be revoked! Also note that finAPI regards user identifiers case-insensitive.

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
ExecutePasswordChangeParams body = new ExecutePasswordChangeParams(); // ExecutePasswordChangeParams | 
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
 **body** | [**ExecutePasswordChangeParams**](ExecutePasswordChangeParams.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAuthorizedUser"></a>
# **getAuthorizedUser**
> User getAuthorizedUser()

Get the authorized user

Get the authorized user&#39;s data. Must pass the user&#39;s access_token. Only the authorized user can get his data, i.e. his access_token must be used. This service returns the user&#39;s password as &#39;XXXXX&#39;.

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
    User result = apiInstance.getAuthorizedUser();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#getAuthorizedUser");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**User**](User.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getVerificationStatus"></a>
# **getVerificationStatus**
> VerificationStatusResource getVerificationStatus(userId)

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
    VerificationStatusResource result = apiInstance.getVerificationStatus(userId);
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

[**VerificationStatusResource**](VerificationStatusResource.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="requestPasswordChange"></a>
# **requestPasswordChange**
> PasswordChangingResource requestPasswordChange(body)

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
RequestPasswordChangeParams body = new RequestPasswordChangeParams(); // RequestPasswordChangeParams | 
try {
    PasswordChangingResource result = apiInstance.requestPasswordChange(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#requestPasswordChange");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestPasswordChangeParams**](RequestPasswordChangeParams.md)|  | [optional]

### Return type

[**PasswordChangingResource**](PasswordChangingResource.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="verifyUser"></a>
# **verifyUser**
> verifyUser(userId)

Verify a user

Verify a user. User verification is only required when your client does not have auto-verification enabled (see field &#39;isUserAutoVerificationEnabled&#39; in Client Configuration Resource). Must pass your global (i.e. client) access_token. Note that finAPI regards user identifiers case-insensitive.

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

