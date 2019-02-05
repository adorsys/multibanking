# MandatorAdministrationApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**changeClientCredentials**](MandatorAdministrationApi.md#changeClientCredentials) | **POST** /api/v1/mandatorAdmin/changeClientCredentials | Change client credentials
[**createIbanRules**](MandatorAdministrationApi.md#createIbanRules) | **POST** /api/v1/mandatorAdmin/ibanRules | Create IBAN rules
[**createKeywordRules**](MandatorAdministrationApi.md#createKeywordRules) | **POST** /api/v1/mandatorAdmin/keywordRules | Create keyword rules
[**deleteIbanRules**](MandatorAdministrationApi.md#deleteIbanRules) | **POST** /api/v1/mandatorAdmin/ibanRules/delete | Delete IBAN rules
[**deleteKeywordRules**](MandatorAdministrationApi.md#deleteKeywordRules) | **POST** /api/v1/mandatorAdmin/keywordRules/delete | Delete keyword rules
[**deleteUsers**](MandatorAdministrationApi.md#deleteUsers) | **POST** /api/v1/mandatorAdmin/deleteUsers | Delete users
[**getIbanRuleList**](MandatorAdministrationApi.md#getIbanRuleList) | **GET** /api/v1/mandatorAdmin/ibanRules | Get IBAN rules
[**getKeywordRuleList**](MandatorAdministrationApi.md#getKeywordRuleList) | **GET** /api/v1/mandatorAdmin/keywordRules | Get keyword rules
[**getUserList**](MandatorAdministrationApi.md#getUserList) | **GET** /api/v1/mandatorAdmin/getUserList | Get user list


<a name="changeClientCredentials"></a>
# **changeClientCredentials**
> changeClientCredentials(body)

Change client credentials

Change the client_secret for any of your clients, including the mandator admin client. Must pass the &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115003661827-Difference-between-app-clients-and-mandator-admin-client&#39;&gt;mandator admin client&lt;/a&gt;&#39;s access_token. &lt;br/&gt;&lt;br/&gt;NOTES:&lt;br/&gt;&amp;bull; When you change a client&#39;s secret, then all of its existing access tokens will be revoked. User access tokens are not affected.&lt;br/&gt;&amp;bull; finAPI is storing client secrets with a one-way encryption. A lost client secret can NOT be recovered.

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
ChangeClientCredentialsParams body = new ChangeClientCredentialsParams(); // ChangeClientCredentialsParams | Parameters for changing client credentials
try {
    apiInstance.changeClientCredentials(body);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#changeClientCredentials");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ChangeClientCredentialsParams**](ChangeClientCredentialsParams.md)| Parameters for changing client credentials |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="createIbanRules"></a>
# **createIbanRules**
> IbanRuleList createIbanRules(body)

Create IBAN rules

This service can be used to define IBAN rules for finAPI&#39;s transaction categorization system. The transaction categorization is run automatically whenever new transactions are imported, as well as when you call the services &#39;Check categorization&#39; or &#39;Trigger categorization&#39;. &lt;br/&gt;&lt;br/&gt;An IBAN rule maps an IBAN to a certain category. finAPI&#39;s categorization system will pick the category as a candidate for any transaction whose counterpart&#39;s account matches the IBAN. It is not guaranteed though that this candidate will actually be applied, as there could be other categorization rules that have higher priority or that are an even better match for the transaction.&lt;br/&gt;&lt;br/&gt;Note that the rules that you define here will be applied to all of your users. They have higher priority than finAPI&#39;s default categorization rules, but lower priority than user-specific rules (User-specific rules are created implicitly whenever a category is manually assigned to a transaction via the PATCH /transactions services). IBAN rules have a higher priority than keyword rules (see the &#39;Create keyword rules&#39; service).

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
IbanRulesParams body = new IbanRulesParams(); // IbanRulesParams | IBAN rule definitions
try {
    IbanRuleList result = apiInstance.createIbanRules(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#createIbanRules");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**IbanRulesParams**](IbanRulesParams.md)| IBAN rule definitions |

### Return type

[**IbanRuleList**](IbanRuleList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="createKeywordRules"></a>
# **createKeywordRules**
> KeywordRuleList createKeywordRules(body)

Create keyword rules

This service can be used to define keyword rules for finAPI&#39;s transaction categorization system. The transaction categorization is run automatically whenever new transactions are imported, as well as when you call the services &#39;Check categorization&#39; or &#39;Trigger categorization&#39;. &lt;br/&gt;&lt;br/&gt;A keyword rule maps a set of keywords to a certain category. finAPI&#39;s categorization system will pick the category as a candidate for any transaction that contains at least one of the defined keywords in its purpose or counterpart information. It is not guaranteed though that this candidate will actually be applied, as there could be other categorization rules that have higher priority or that are an even better match for the transaction. If there are multiple keyword rules that match a transaction, finAPI will pick the one with the highest count of matched keywords.&lt;br/&gt;&lt;br/&gt;Note that the rules that you define here will be applied to all of your users. They have higher priority than finAPI&#39;s default categorization rules, but lower priority than user-specific rules (User-specific rules are created implicitly whenever a category is manually assigned to a transaction via the PATCH /transactions services). Keyword rules have a lower priority than IBAN rules (see the &#39;Create IBAN rules&#39; service).

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
KeywordRulesParams body = new KeywordRulesParams(); // KeywordRulesParams | Keyword rule definitions
try {
    KeywordRuleList result = apiInstance.createKeywordRules(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#createKeywordRules");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**KeywordRulesParams**](KeywordRulesParams.md)| Keyword rule definitions |

### Return type

[**KeywordRuleList**](KeywordRuleList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteIbanRules"></a>
# **deleteIbanRules**
> IdentifierList deleteIbanRules(body)

Delete IBAN rules

Delete one or multiple IBAN rules that you have previously created via the &#39;Create IBAN rules&#39; service.

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
IdentifiersParams body = new IdentifiersParams(); // IdentifiersParams | List of IBAN rules identifiers.The maximum number of identifiers is 100.
try {
    IdentifierList result = apiInstance.deleteIbanRules(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#deleteIbanRules");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**IdentifiersParams**](IdentifiersParams.md)| List of IBAN rules identifiers.The maximum number of identifiers is 100. |

### Return type

[**IdentifierList**](IdentifierList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteKeywordRules"></a>
# **deleteKeywordRules**
> IdentifierList deleteKeywordRules(body)

Delete keyword rules

Delete one or multiple keyword rules that you have previously created via the &#39;Create keyword rules&#39; service.

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
IdentifiersParams body = new IdentifiersParams(); // IdentifiersParams | List of keyword rule identifiers.The maximum number of identifiers is 100.
try {
    IdentifierList result = apiInstance.deleteKeywordRules(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#deleteKeywordRules");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**IdentifiersParams**](IdentifiersParams.md)| List of keyword rule identifiers.The maximum number of identifiers is 100. |

### Return type

[**IdentifierList**](IdentifierList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="deleteUsers"></a>
# **deleteUsers**
> UserIdentifiersList deleteUsers(body)

Delete users

Delete one or several users, which are specified by a given list of identifiers. Must pass the &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115003661827-Difference-between-app-clients-and-mandator-admin-client&#39; target&#x3D;&#39;_blank&#39;&gt;mandator admin client&lt;/a&gt;&#39;s access_token. &lt;br/&gt;&lt;br/&gt;&lt;b&gt;NOTE&lt;/b&gt;: finAPI may fail to delete one (or several, or all) of the specified users. A user cannot get deleted when his data is currently locked by an internal process (for instance, update of a bank connection or transactions categorization). The response contains the identifiers of all users that could not get deleted, and all users that could get deleted, separated in two lists. The mandator admin client can retry the request at a later time for the users who could not get deleted.

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
UserIdentifiersParams body = new UserIdentifiersParams(); // UserIdentifiersParams | List of user identifiers
try {
    UserIdentifiersList result = apiInstance.deleteUsers(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#deleteUsers");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**UserIdentifiersParams**](UserIdentifiersParams.md)| List of user identifiers |

### Return type

[**UserIdentifiersList**](UserIdentifiersList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getIbanRuleList"></a>
# **getIbanRuleList**
> PageableIbanRuleList getIbanRuleList(page, perPage)

Get IBAN rules

Returns all IBAN-based categorization rules that you have defined for your users via the &#39;Create IBAN rules&#39; service.

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
Integer page = 1; // Integer | Result page that you want to retrieve
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
try {
    PageableIbanRuleList result = apiInstance.getIbanRuleList(page, perPage);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#getIbanRuleList");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **page** | **Integer**| Result page that you want to retrieve | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]

### Return type

[**PageableIbanRuleList**](PageableIbanRuleList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getKeywordRuleList"></a>
# **getKeywordRuleList**
> PageableKeywordRuleList getKeywordRuleList(page, perPage)

Get keyword rules

Returns all keyword-based categorization rules that you have defined for your users via the &#39;Create keyword rules&#39; service.

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
Integer page = 1; // Integer | Result page that you want to retrieve
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
try {
    PageableKeywordRuleList result = apiInstance.getKeywordRuleList(page, perPage);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling MandatorAdministrationApi#getKeywordRuleList");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **page** | **Integer**| Result page that you want to retrieve | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]

### Return type

[**PageableKeywordRuleList**](PageableKeywordRuleList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getUserList"></a>
# **getUserList**
> PageableUserInfoList getUserList(minRegistrationDate, maxRegistrationDate, minDeletionDate, maxDeletionDate, minLastActiveDate, maxLastActiveDate, includeMonthlyStats, monthlyStatsStartDate, monthlyStatsEndDate, minBankConnectionCountInMonthlyStats, isDeleted, page, perPage, order)

Get user list

&lt;p&gt;Get a list of the users of the mandator that is authorized by the access_token. Must pass the &lt;a href&#x3D;&#39;https://finapi.zendesk.com/hc/en-us/articles/115003661827-Difference-between-app-clients-and-mandator-admin-client&#39; target&#x3D;&#39;_blank&#39;&gt;mandator admin client&lt;/a&gt;&#39;s access_token. You can set optional search criteria to get only those users that you are interested in. If you do not specify any search criteria, then this service functions as a &#39;get all&#39; service.&lt;/p&gt;&lt;p&gt;Note that the original user id is no longer available in finAPI once a user has been deleted. Because of this, the userId of deleted users will be a distorted version of the original userId. For example, if the deleted user&#39;s id was originally &#39;user&#39;, then this service will return &#39;uXXr&#39; as the userId.&lt;/p&gt;

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
Boolean includeMonthlyStats = false; // Boolean | Whether to include the 'monthlyStats' for the returned users. If not specified, then the field defaults to 'false'.
String monthlyStatsStartDate = "monthlyStatsStartDate_example"; // String | Minimum bound for the monthly stats (=oldest month that should be included). Must be passed in the format 'YYYY-MM'. If not specified, then the monthly stats will go back up to the first month in which the user existed (date of the user's registration). Note that this field is only regarded if 'includeMonthlyStats' = true.
String monthlyStatsEndDate = "monthlyStatsEndDate_example"; // String | Maximum bound for the monthly stats (=latest month that should be included). Must be passed in the format 'YYYY-MM'. If not specified, then the monthly stats will go up to either the current month (for active users), or up to the month of deletion (for deleted users). Note that this field is only regarded if  'includeMonthlyStats' = true.
Integer minBankConnectionCountInMonthlyStats = 0; // Integer | A value of X means that the service will return only those users which had at least X bank connections imported at any time within the returned monthly stats set. This field is only regarded when 'includeMonthlyStats' is set to 'true'. The default value for this field is 0.
Boolean isDeleted = true; // Boolean | If NOT specified, then the service will regard both active and deleted users in the search. If set to 'true', then ONLY deleted users will be regarded. If set to 'false', then ONLY active users will be regarded.
Integer page = 1; // Integer | Result page that you want to retrieve
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can order the results by 'userId'. The default order for this service is 'userId,asc'. The general format is: 'property[,asc|desc]', with 'asc' being the default value. 
try {
    PageableUserInfoList result = apiInstance.getUserList(minRegistrationDate, maxRegistrationDate, minDeletionDate, maxDeletionDate, minLastActiveDate, maxLastActiveDate, includeMonthlyStats, monthlyStatsStartDate, monthlyStatsEndDate, minBankConnectionCountInMonthlyStats, isDeleted, page, perPage, order);
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
 **includeMonthlyStats** | **Boolean**| Whether to include the &#39;monthlyStats&#39; for the returned users. If not specified, then the field defaults to &#39;false&#39;. | [optional] [default to false]
 **monthlyStatsStartDate** | **String**| Minimum bound for the monthly stats (&#x3D;oldest month that should be included). Must be passed in the format &#39;YYYY-MM&#39;. If not specified, then the monthly stats will go back up to the first month in which the user existed (date of the user&#39;s registration). Note that this field is only regarded if &#39;includeMonthlyStats&#39; &#x3D; true. | [optional]
 **monthlyStatsEndDate** | **String**| Maximum bound for the monthly stats (&#x3D;latest month that should be included). Must be passed in the format &#39;YYYY-MM&#39;. If not specified, then the monthly stats will go up to either the current month (for active users), or up to the month of deletion (for deleted users). Note that this field is only regarded if  &#39;includeMonthlyStats&#39; &#x3D; true. | [optional]
 **minBankConnectionCountInMonthlyStats** | **Integer**| A value of X means that the service will return only those users which had at least X bank connections imported at any time within the returned monthly stats set. This field is only regarded when &#39;includeMonthlyStats&#39; is set to &#39;true&#39;. The default value for this field is 0. | [optional] [default to 0]
 **isDeleted** | **Boolean**| If NOT specified, then the service will regard both active and deleted users in the search. If set to &#39;true&#39;, then ONLY deleted users will be regarded. If set to &#39;false&#39;, then ONLY active users will be regarded. | [optional]
 **page** | **Integer**| Result page that you want to retrieve | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can order the results by &#39;userId&#39;. The default order for this service is &#39;userId,asc&#39;. The general format is: &#39;property[,asc|desc]&#39;, with &#39;asc&#39; being the default value.  | [optional]

### Return type

[**PageableUserInfoList**](PageableUserInfoList.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

