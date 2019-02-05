# PaymentsApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getPayments**](PaymentsApi.md#getPayments) | **GET** /api/v1/payments | Get payments


<a name="getPayments"></a>
# **getPayments**
> PageablePaymentResources getPayments(ids, accountIds, minAmount, maxAmount, page, perPage, order)

Get payments

Get payments of the user that is authorized by the access_token. &lt;p&gt;Note: For requesting / executing payments, please refer to the &#39;Accounts&#39; section of the API.&lt;/p&gt;

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.PaymentsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

PaymentsApi apiInstance = new PaymentsApi();
List<Long> ids = Arrays.asList(56L); // List<Long> | A comma-separated list of payment identifiers. If specified, then only payments whose identifier is matching any of the given identifiers will be regarded. The maximum number of identifiers is 1000.
List<Long> accountIds = Arrays.asList(56L); // List<Long> | A comma-separated list of account identifiers. If specified, then only payments that relate to the given account(s) will be regarded. The maximum number of identifiers is 1000.
BigDecimal minAmount = new BigDecimal(); // BigDecimal | If specified, then only those payments are regarded whose (absolute) total amount is equal or greater than the given amount will be regarded. The value must be a positive (absolute) amount.
BigDecimal maxAmount = new BigDecimal(); // BigDecimal | If specified, then only those payments are regarded whose (absolute) total amount is equal or less than the given amount will be regarded. Value must be a positive (absolute) amount.
Integer page = 1; // Integer | Result page that you want to retrieve
Integer perPage = 20; // Integer | Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes.
List<String> order = Arrays.asList("order_example"); // List<String> | Determines the order of the results. You can use the following fields for ordering the response: 'id', 'amount', 'requestDate' and 'executionDate'. The default order for all services is 'id,asc'.
try {
    PageablePaymentResources result = apiInstance.getPayments(ids, accountIds, minAmount, maxAmount, page, perPage, order);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling PaymentsApi#getPayments");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ids** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of payment identifiers. If specified, then only payments whose identifier is matching any of the given identifiers will be regarded. The maximum number of identifiers is 1000. | [optional]
 **accountIds** | [**List&lt;Long&gt;**](Long.md)| A comma-separated list of account identifiers. If specified, then only payments that relate to the given account(s) will be regarded. The maximum number of identifiers is 1000. | [optional]
 **minAmount** | **BigDecimal**| If specified, then only those payments are regarded whose (absolute) total amount is equal or greater than the given amount will be regarded. The value must be a positive (absolute) amount. | [optional]
 **maxAmount** | **BigDecimal**| If specified, then only those payments are regarded whose (absolute) total amount is equal or less than the given amount will be regarded. Value must be a positive (absolute) amount. | [optional]
 **page** | **Integer**| Result page that you want to retrieve | [optional] [default to 1]
 **perPage** | **Integer**| Maximum number of records per page. Can be at most 500. NOTE: Due to its validation and visualization, the swagger frontend might show very low performance, or even crashes, when a service responds with a lot of data. It is recommended to use a HTTP client like Postman or DHC instead of our swagger frontend for service calls with large page sizes. | [optional] [default to 20]
 **order** | [**List&lt;String&gt;**](String.md)| Determines the order of the results. You can use the following fields for ordering the response: &#39;id&#39;, &#39;amount&#39;, &#39;requestDate&#39; and &#39;executionDate&#39;. The default order for all services is &#39;id,asc&#39;. | [optional]

### Return type

[**PageablePaymentResources**](PageablePaymentResources.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

