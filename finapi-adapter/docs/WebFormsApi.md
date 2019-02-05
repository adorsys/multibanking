# WebFormsApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getWebForm**](WebFormsApi.md#getWebForm) | **GET** /api/v1/webForms/{id} | Get a web form


<a name="getWebForm"></a>
# **getWebForm**
> WebForm getWebForm(id)

Get a web form

Get a web form of the user that is authorized by the access_token. Must pass the web form&#39;s identifier and the user&#39;s access_token. &lt;br/&gt;&lt;br/&gt;Note that every web form resource is automatically removed from the finAPI system after 24 hours after its creation.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.WebFormsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: finapi_auth
OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

WebFormsApi apiInstance = new WebFormsApi();
Long id = 789L; // Long | Identifier of web form
try {
    WebForm result = apiInstance.getWebForm(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling WebFormsApi#getWebForm");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**| Identifier of web form |

### Return type

[**WebForm**](WebForm.md)

### Authorization

[finapi_auth](../README.md#finapi_auth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

