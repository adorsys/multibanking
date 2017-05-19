
# PasswordChangingResource

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**userId** | **String** | User identifier | 
**userEmail** | **String** | User&#39;s email, encrypted. Decrypt with your data decryption key. If the user has no email set, then this field will be null. |  [optional]
**passwordChangeToken** | **String** | Encrypted password change token. Decrypt this token with your data decryption key, and pass the decrypted token to the /users/executePasswordChange service in order to set a new password for the user. | 



