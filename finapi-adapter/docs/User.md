
# User

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | User identifier | 
**password** | **String** | User&#39;s password. Please note that some services may return a distorted password (always &#39;XXXXX&#39;). See the documentation of individual services to find out whether the password is returned as plain text or as &#39;XXXXX&#39;. | 
**email** | **String** | User&#39;s email address |  [optional]
**phone** | **String** | User&#39;s phone number |  [optional]
**isAutoUpdateEnabled** | **Boolean** | Whether the user&#39;s bank connections will get updated in the course of finAPI&#39;s automatic batch update. Note that the automatic batch update will only update bank connections where all of the following applies:&lt;br&gt;&lt;br&gt; - the PIN is stored in finAPI for the bank connection, and the related bank does not have volatile PINs (see the ‘pinsAreVolatile’ flag of the Bank Resource)&lt;br&gt; - the user has accepted the latest Terms and Conditions (this only applies to users whose mandator has PISP license or doesn&#39;t have a license at all)&lt;br&gt; - the user has allowed finAPI to use his old stored credentials (this only applies to users which had stored credentials before introducing a web form feature and whose mandator has PISP license or doesn&#39;t have a license at all)&lt;br&gt; - the previous update using the stored credentials did not fail due to the credentials being incorrect (or there was no previous update with the stored credentials)&lt;br&gt; - the bank that the bank connection relates to is included in the automatic batch update (please contact your Sys-Admin for details about the batch update configuration)&lt;br&gt; - at least one of the bank&#39;s supported data sources can be used by finAPI for your client (i.e.: if a bank supports only web scraping, but web scraping is disabled for your client, then bank connections of that bank will not get updated by the automatic batch update)&lt;br&gt;&lt;br&gt;Also note that the automatic batch update must generally be enabled for your client in order for this field to have any effect.&lt;br/&gt;&lt;br/&gt;WARNING: The automatic update will always download transactions and security positions for any account that it updates! This means that the user will no longer be able to download just the balances for his accounts once the automatic update has run (The &#39;skipPositionsDownload&#39; flag in the bankConnections/update service can no longer be set to true). | 



