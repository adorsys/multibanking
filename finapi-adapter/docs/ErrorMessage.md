
# ErrorMessage

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**errors** | [**List&lt;ErrorDetails&gt;**](ErrorDetails.md) | List of errors | 
**date** | **String** | Server date of when the error(s) occurred, in the format YYYY-MM-DD HH:MM:SS.SSS | 
**requestId** | **String** | ID of the request that caused this error. This is either what you have passed for the header &#39;X-REQUEST-ID&#39;, or an auto-generated ID in case you didn&#39;t pass any value for that header. |  [optional]
**endpoint** | **String** | The service endpoint that was called | 
**authContext** | **String** | Information about the authorization context of the service call | 
**bank** | **String** | BLZ and name (in format \&quot;&lt;BLZ&gt; - &lt;name&gt;\&quot;) of a bank that was used for the original request |  [optional]



