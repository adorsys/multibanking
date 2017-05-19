
# Apiv1testsmockBatchUpdateNewTransactions

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**amount** | [**BigDecimal**](BigDecimal.md) | Amount. Required. | 
**purpose** | **String** | Purpose. Maximum length is 2000. Optional. Default value: null. |  [optional]
**counterpart** | **String** | Counterpart. Maximum length is 270. Optional. Default value: null. |  [optional]
**counterpartIban** | **String** | Counterpart IBAN. Optional. Default value: null. |  [optional]
**counterpartBlz** | **String** | Counterpart BLZ. Optional. Default value: null. |  [optional]
**counterpartBic** | **String** | Counterpart BIC. Optional. Default value: null. |  [optional]
**bookingDate** | **String** | Booking date in the format &#39;yyyy-MM-dd&#39;.&lt;br/&gt;&lt;br/&gt;If the date lies back more than 10 days from the booking date of the latest transaction that currently exists in the account, then this transaction will be ignored and not imported. If the date depicts a date in the future, then finAPI will deal with it the same way as it does with real transactions during a real update (see fields &#39;bankBookingDate&#39; and &#39;finapiBookingDate&#39; in the Transaction Resource for explanation).&lt;br/&gt;&lt;br/&gt;This field is optional, default value is the current date. |  [optional]
**valueDate** | **String** | Value date in the format &#39;yyyy-MM-dd&#39;. Optional. Default value: Same as the booking date. |  [optional]



