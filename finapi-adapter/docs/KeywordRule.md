
# KeywordRule

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Rule identifier | 
**category** | [**Category**](Category.md) | The category that this rule assigns to the transactions that it matches | 
**direction** | [**DirectionEnum**](#DirectionEnum) | Direction for the rule. &#39;Income&#39; means that the rule applies to transactions with a positive amount only, &#39;Spending&#39; means it applies to transactions with a negative amount only. | 
**creationDate** | **String** | Timestamp of when the rule was created, in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time) | 
**keywords** | **List&lt;String&gt;** | Set of keywords that this rule defines. | 


<a name="DirectionEnum"></a>
## Enum: DirectionEnum
Name | Value
---- | -----
INCOME | &quot;Income&quot;
SPENDING | &quot;Spending&quot;



