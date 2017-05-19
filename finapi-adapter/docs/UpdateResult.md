
# UpdateResult

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**result** | [**ResultEnum**](#ResultEnum) | Note that &#39;OK&#39; just means that finAPI could successfully connect and log in to the bank server. However, it does not necessarily mean that all accounts could be updated successfully. For the latter, please refer to the &#39;status&#39; field of the Account resource. | 
**errorMessage** | **String** | In case the update result is not &lt;code&gt;OK&lt;/code&gt;, this field may contain an error message with details about why the update failed (it is not guaranteed that a message is available though). In case the update result is &lt;code&gt;OK&lt;/code&gt;, the field will always be null. |  [optional]
**errorType** | [**ErrorTypeEnum**](#ErrorTypeEnum) | In case the update result is not &lt;code&gt;OK&lt;/code&gt;, this field contains the type of the error that occurred. BUSINESS means that the bank server responded with a non-technical error message for the user. TECHNICAL means that some internal error has occurred in finAPI or at the bank server. |  [optional]
**timestamp** | **String** | Time of the update. The value is returned in the format &#39;yyyy-MM-dd HH:mm:ss.SSS&#39; (german time). | 


<a name="ResultEnum"></a>
## Enum: ResultEnum
Name | Value
---- | -----
OK | &quot;OK&quot;
BANK_SERVER_REJECTION | &quot;BANK_SERVER_REJECTION&quot;
INTERNAL_SERVER_ERROR | &quot;INTERNAL_SERVER_ERROR&quot;


<a name="ErrorTypeEnum"></a>
## Enum: ErrorTypeEnum
Name | Value
---- | -----
BUSINESS | &quot;BUSINESS&quot;
TECHNICAL | &quot;TECHNICAL&quot;



