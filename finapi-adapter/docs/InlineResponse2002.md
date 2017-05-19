
# InlineResponse2002

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**latestCommonBalanceTimestamp** | **String** | The latestCommonBalanceTimestamp is the latest timestamp at which all given accounts have been up to date. Only balances with their date being smaller than the latestCommonBalanceTimestamp are reliable. Example: A user has two accounts: A (last update today, so balance from today) and B (last update yesterday, so balance from yesterday). The service /accounts/dailyBalances will return a balance for yesterday and for today, with the info latestCommonBalanceTimestamp&#x3D;yesterday. Since account B might have received transactions this morning, today&#39;s balance might be wrong. So either make sure that all selected accounts are up to date before calling this service, or use the results carefully in combination with the latestCommonBalanceTimestamp. The format is &#39;yyyy-MM-dd HH:mm:ss.SSS&#39; (german time). |  [optional]
**dailyBalances** | [**List&lt;InlineResponse2002DailyBalances&gt;**](InlineResponse2002DailyBalances.md) | List of daily balances for specified accounts and dates range | 
**paging** | [**InlineResponse2002Paging**](InlineResponse2002Paging.md) |  |  [optional]



