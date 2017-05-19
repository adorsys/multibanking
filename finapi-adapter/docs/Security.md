
# Security

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Identifier. Note: Whenever a security account is being updated, its security positions will be internally re-created, meaning that the identifier of a security position might change over time. | 
**accountId** | **Long** | Security account identifier | 
**name** | **String** | Name |  [optional]
**isin** | **String** | ISIN |  [optional]
**wkn** | **String** | WKN |  [optional]
**quote** | [**BigDecimal**](BigDecimal.md) | Quote |  [optional]
**quoteCurrency** | **String** | Currency of quote |  [optional]
**quoteType** | [**QuoteTypeEnum**](#QuoteTypeEnum) | Type of quote. &#39;PERC&#39; if quote is a percentage value, &#39;ACTU&#39; if quote is the actual amount |  [optional]
**quoteDate** | **String** | Quote date |  [optional]
**quantityNominal** | [**BigDecimal**](BigDecimal.md) | Value of quantity or nominal |  [optional]
**quantityNominalType** | [**QuantityNominalTypeEnum**](#QuantityNominalTypeEnum) | Type of quantity or nominal value. &#39;UNIT&#39; if value is a quantity, &#39;FAMT&#39; if value is the nominal amount |  [optional]
**marketValue** | [**BigDecimal**](BigDecimal.md) | Market value |  [optional]
**entryQuote** | [**BigDecimal**](BigDecimal.md) | Entry quote |  [optional]
**entryQuoteCurrency** | **String** | Currency of entry quote |  [optional]


<a name="QuoteTypeEnum"></a>
## Enum: QuoteTypeEnum
Name | Value
---- | -----
ACTU | &quot;ACTU&quot;
PERC | &quot;PERC&quot;


<a name="QuantityNominalTypeEnum"></a>
## Enum: QuantityNominalTypeEnum
Name | Value
---- | -----
UNIT | &quot;UNIT&quot;
FAMT | &quot;FAMT&quot;



