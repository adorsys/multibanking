# MocksAndTestsApi

All URIs are relative to *https://localhost/*

Method | HTTP request | Description
------------- | ------------- | -------------
[**mockBatchUpdate**](MocksAndTestsApi.md#mockBatchUpdate) | **POST** /api/v1/tests/mockBatchUpdate | Mock batch update


<a name="mockBatchUpdate"></a>
# **mockBatchUpdate**
> mockBatchUpdate(body)

Mock batch update

This service can be used to mock an update of one or several bank connections by letting you simulate finAPI&#39;s communication with a bank server. More specifically, you can provide custom balances and transactions for existing accounts and finAPI will import that data into the accounts as if the data had been delivered by a real bank server during a real update. The idea of this service is to allow you to create accounts with specific data in them so that you can test your application in different scenarios.&lt;br/&gt;&lt;br/&gt;You can also test your application&#39;s reception and processing of push notifications with this service, by enabling the &#39;triggerNotifications&#39; flag in your request. When this flag is enabled, finAPI will send notifications to your application based on the notification rules that are set up for the user and on the data you provided in the request, the same way as it works with finAPI&#39;s real automatic batch update process.&lt;br/&gt;&lt;br/&gt;Note that this service behaves mostly like calling the bank connection update service, meaning that it returns immediately after having asynchronously started the update process, and also meaning that you have to check the status of the updated bank connections and accounts to find out when the update has finished and what the result is. As you can update several bank connections at once, this service is closer to how finAPI&#39;s automatic batch updates work as it is to the manual update service though. Because of this, the result of the mocked bank connection updates will be stored in the &#39;lastAutoUpdate&#39; field of the bank connections and not in the &#39;lastManualUpdate&#39; field. Also, just like with the real batch update, any bank connection that you use with this service must have a PIN stored (even though it is not actually forwarded to any bank server).&lt;br/&gt;&lt;br/&gt;Also note that this service may be called only when the user&#39;s automatic bank connection updates are disabled, to make sure that the mock updates cannot intervene with a real update (please see the User field &#39;isAutoUpdateEnabled&#39;). Also, it is not possible to use the demo bank connection in this service, so you need to have at least one real online bank connection. At last, it is currently not possible to mock data for security accounts with this service, as you can only pass transactions, but not security positions.&lt;br/&gt;&lt;br/&gt;Please be aware that you will &#39;mess up&#39; the accounts when using this service, meaning that when you perform a real update of accounts that you have previously updated with this service, finAPI might detect inconsistencies in the data that exists in its database and the data that is reported by the bank server, and try to fix this with the insertion of a &#39;Zwischensaldo&#39; transaction. Also, new real transactions might not get imported as finAPI could match them to mocked transactions. &lt;b&gt;THIS SERVICE IS MEANT FOR TESTING PURPOSES DURING DEVELOPMENT OF YOUR APPLICATION ONLY!&lt;/b&gt; This is why it will work only on the sandbox or alpha environments. Calling it on the live environment will result in &lt;b&gt;403 Forbidden&lt;/b&gt;.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.MocksAndTestsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

MocksAndTestsApi apiInstance = new MocksAndTestsApi();
Body12 body = new Body12(); // Body12 | Data for mock bank connection updates
try {
    apiInstance.mockBatchUpdate(body);
} catch (ApiException e) {
    System.err.println("Exception when calling MocksAndTestsApi#mockBatchUpdate");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body12**](Body12.md)| Data for mock bank connection updates |

### Return type

null (empty response body)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

