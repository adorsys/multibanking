
# InlineResponse403Errors

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**message** | **String** | Error message |  [optional]
**code** | [**CodeEnum**](#CodeEnum) | Error code. See the documentation of the individual services for details about what values may be returned. |  [optional]
**type** | [**TypeEnum**](#TypeEnum) | Error type. BUSINESS errors depict German error messages for the user, e.g. from a bank server. TECHNICAL errors depict internal errors. |  [optional]


<a name="CodeEnum"></a>
## Enum: CodeEnum
Name | Value
---- | -----
MISSING_FIELD | &quot;MISSING_FIELD&quot;
UNKNOWN_ENTITY | &quot;UNKNOWN_ENTITY&quot;
METHOD_NOT_ALLOWED | &quot;METHOD_NOT_ALLOWED&quot;
ENTITY_EXISTS | &quot;ENTITY_EXISTS&quot;
ILLEGAL_ENTITY_STATE | &quot;ILLEGAL_ENTITY_STATE&quot;
UNEXPECTED_ERROR | &quot;UNEXPECTED_ERROR&quot;
ILLEGAL_FIELD_VALUE | &quot;ILLEGAL_FIELD_VALUE&quot;
UNAUTHORIZED_ACCESS | &quot;UNAUTHORIZED_ACCESS&quot;
BAD_REQUEST | &quot;BAD_REQUEST&quot;
UNSUPPORTED_ORDER | &quot;UNSUPPORTED_ORDER&quot;
ILLEGAL_PAGE_SIZE | &quot;ILLEGAL_PAGE_SIZE&quot;
INVALID_FILTER_OPTIONS | &quot;INVALID_FILTER_OPTIONS&quot;
TOO_MANY_IDS | &quot;TOO_MANY_IDS&quot;
BANK_SERVER_REJECTION | &quot;BANK_SERVER_REJECTION&quot;
IBAN_ONLY_MONEY_TRANSFER_NOT_SUPPORTED | &quot;IBAN_ONLY_MONEY_TRANSFER_NOT_SUPPORTED&quot;
COLLECTIVE_MONEY_TRANSFER_NOT_SUPPORTED | &quot;COLLECTIVE_MONEY_TRANSFER_NOT_SUPPORTED&quot;
INVALID_TWO_STEP_PROCEDURE | &quot;INVALID_TWO_STEP_PROCEDURE&quot;
MISSING_TWO_STEP_PROCEDURE | &quot;MISSING_TWO_STEP_PROCEDURE&quot;
UNSUPPORTED_MEDIA_TYPE | &quot;UNSUPPORTED_MEDIA_TYPE&quot;
UNSUPPORTED_BANK | &quot;UNSUPPORTED_BANK&quot;
USER_NOT_VERIFIED | &quot;USER_NOT_VERIFIED&quot;
USER_ALREADY_VERIFIED | &quot;USER_ALREADY_VERIFIED&quot;
INVALID_TOKEN | &quot;INVALID_TOKEN&quot;
LOCKED | &quot;LOCKED&quot;
NO_ACCOUNTS_FOR_TYPE_LIST | &quot;NO_ACCOUNTS_FOR_TYPE_LIST&quot;
FORBIDDEN | &quot;FORBIDDEN&quot;


<a name="TypeEnum"></a>
## Enum: TypeEnum
Name | Value
---- | -----
BUSINESS | &quot;BUSINESS&quot;
TECHNICAL | &quot;TECHNICAL&quot;



