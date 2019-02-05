
# AccessToken

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**refreshToken** | **String** | Refresh token. Only set in case of grant_type&#x3D;&#39;password&#39;. Token has a length of up to 128 characters. |  [optional]
**expiresIn** | **Integer** | Expiration time in seconds. A value of 0 means that the token never expires (unless it is explicitly invalidated, e.g. by revocation, or when a user gets locked). | 
**tokenType** | **String** | Token type (it&#39;s always &#39;bearer&#39;) | 
**scope** | **String** | Requested scopes (it&#39;s always &#39;all&#39;) | 
**accessToken** | **String** | Access token. Token has a length of up to 128 characters. | 



