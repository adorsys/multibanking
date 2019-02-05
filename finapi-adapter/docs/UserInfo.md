
# UserInfo

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**userId** | **String** | User&#39;s identifier | 
**registrationDate** | **String** | User&#39;s registration date, in the format &#39;YYYY-MM-DD&#39; | 
**deletionDate** | **String** | User&#39;s deletion date, in the format &#39;YYYY-MM-DD&#39;. May be null if the user has not been deleted. |  [optional]
**lastActiveDate** | **String** | User&#39;s last active date, in the format &#39;YYYY-MM-DD&#39;. May be null if the user has not yet logged in. |  [optional]
**bankConnectionCount** | **Integer** | Number of bank connections that currently exist for this user. | 
**latestBankConnectionImportDate** | **String** | Latest date of when a bank connection was imported for this user, in the format &#39;YYYY-MM-DD&#39;. This field is null when there has never been a bank connection import. |  [optional]
**latestBankConnectionDeletionDate** | **String** | Latest date of when a bank connection was deleted for this user, in the format &#39;YYYY-MM-DD&#39;. This field is null when there has never been a bank connection deletion. |  [optional]
**monthlyStats** | [**List&lt;MonthlyUserStats&gt;**](MonthlyUserStats.md) | Additional information about the user&#39;s data or activities, broken down in months. The list will by default contain an entry for each month starting with the month of when the user was registered, up to the current month. The date range may vary when you have limited it in the request. &lt;br/&gt;&lt;br/&gt;Please note:&lt;br/&gt;&amp;bull; this field is only set when &#39;includeMonthlyStats&#39; &#x3D; true, otherwise it will be null.&lt;br/&gt;&amp;bull; the list is always ordered from the latest month first, to the oldest month last.&lt;br/&gt;&amp;bull; the list will never contain an entry for a month that was prior to the month of when the user was registered, or after the month of when the user was deleted, even when you have explicitly set a respective date range. This means that the list may be empty if you are requesting a date range where the user didn&#39;t exist yet, or didn&#39;t exist any longer. |  [optional]



